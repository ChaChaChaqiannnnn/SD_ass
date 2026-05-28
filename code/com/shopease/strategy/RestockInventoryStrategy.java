package com.shopease.strategy;

import com.shopease.model.Admin;
import com.shopease.model.AdminInventoryLog;
import com.shopease.model.Product;
import com.shopease.model.User;

/**
 * Strategy Pattern — adds stock for a product (Restock selected / Quick +10).
 */
public class RestockInventoryStrategy implements ShopEaseInventoryActionStrategy {

    @Override
    public boolean execute(InventoryActionContext ctx, InventoryActionResult result) {
        return adjustStock(ctx, result, ctx.amount, "RESTOCK", "");
    }

    static boolean adjustStock(InventoryActionContext ctx,
                               InventoryActionResult result,
                               int quantityChange,
                               String actionType,
                               String remarks) {
        if (!(ctx.currentUser instanceof Admin)) {
            result.message = "Only admins can change stock.";
            return false;
        }
        if (quantityChange == 0) {
            result.message = "Change amount cannot be zero.";
            return false;
        }
        Product product = ctx.productDAO.getProductById(ctx.productId);
        if (product == null) {
            result.message = "Product not found.";
            return false;
        }

        int stockBefore = product.getStockQuantity();
        int stockAfter = Math.max(0, stockBefore + quantityChange);
        if ("REDUCE".equals(actionType) && quantityChange < 0 && stockAfter == stockBefore) {
            result.message = "Stock is already 0 for " + product.getName() + ".";
            return false;
        }

        ctx.productDAO.updateStock(ctx.productId, stockAfter);
        ctx.inventorySubject.setStock(stockAfter, product.getName());

        AdminInventoryLog log = new AdminInventoryLog(
                ctx.currentUser.getUserId(),
                ctx.currentUser.getName(),
                ctx.productId,
                product.getName(),
                actionType,
                stockAfter - stockBefore,
                stockBefore,
                stockAfter,
                remarks,
                new java.util.Date()
        );
        ctx.adminActivityDAO.insertLog(log);
        result.newLog = log;

        if ("REDUCE".equals(actionType)) {
            result.message = "Reduced " + product.getName() + " by " + (-quantityChange)
                    + " (now " + stockAfter + "). Remark saved.";
        } else {
            result.message = "Restocked " + product.getName() + " by +" + quantityChange
                    + " (now " + stockAfter + ").";
        }
        return true;
    }
}
