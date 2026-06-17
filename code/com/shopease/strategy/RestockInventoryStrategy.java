package com.shopease.strategy;

import com.shopease.model.Admin;
import com.shopease.model.AdminInventoryLog;
import com.shopease.model.Product;
import com.shopease.model.User;

/**
 * GoF Strategy — ConcreteStrategy (restock inventory).
 */
public class RestockInventoryStrategy implements ShopEaseInventoryActionStrategy {

    @Override
    public boolean algorithmInterface(InventoryActionRequest request, InventoryActionResult result) {
        return adjustStock(request, result, request.amount, "RESTOCK", "");
    }

    static boolean adjustStock(InventoryActionRequest request,
                               InventoryActionResult result,
                               int quantityChange,
                               String actionType,
                               String remarks) {
        if (!(request.currentUser instanceof Admin)) {
            result.message = "Only admins can change stock.";
            return false;
        }
        if (quantityChange == 0) {
            result.message = "Change amount cannot be zero.";
            return false;
        }
        Product product = request.productDAO.getProductById(request.productId);
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

        request.productDAO.updateStock(request.productId, stockAfter);
        request.inventorySubject.setStock(stockAfter, product.getName());

        AdminInventoryLog log = new AdminInventoryLog(
                request.currentUser.getUserId(),
                request.currentUser.getName(),
                request.productId,
                product.getName(),
                actionType,
                stockAfter - stockBefore,
                stockBefore,
                stockAfter,
                remarks,
                new java.util.Date()
        );
        request.adminActivityDAO.insertLog(log);
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
