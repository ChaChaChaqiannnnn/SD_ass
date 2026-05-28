package com.shopease.strategy;

import com.shopease.model.Admin;
import com.shopease.model.AdminInventoryLog;
import com.shopease.model.Product;

/**
 * Strategy Pattern — reverts the last admin stock change for the current session.
 */
public class UndoInventoryStrategy implements ShopEaseInventoryActionStrategy {

    @Override
    public boolean execute(InventoryActionContext ctx, InventoryActionResult result) {
        if (!(ctx.currentUser instanceof Admin)) {
            result.message = "Nothing to undo.";
            return false;
        }
        if (ctx.lastUndoableAction == null) {
            result.message = "Nothing to undo.";
            return false;
        }

        AdminInventoryLog previous = ctx.lastUndoableAction;
        Product product = ctx.productDAO.getProductById(previous.getProductId());
        if (product == null) {
            result.message = "Product no longer exists.";
            result.clearedUndoAction = previous;
            return false;
        }

        int currentStock = product.getStockQuantity();
        int restoredStock = previous.getStockBefore();
        ctx.productDAO.updateStock(previous.getProductId(), restoredStock);
        ctx.inventorySubject.setStock(restoredStock, previous.getProductName());

        AdminInventoryLog undoLog = new AdminInventoryLog(
                ctx.currentUser.getUserId(),
                ctx.currentUser.getName(),
                previous.getProductId(),
                previous.getProductName(),
                "UNDO",
                restoredStock - currentStock,
                currentStock,
                restoredStock,
                "Undo " + previous.getActionType()
                        + (previous.getRemarks().isEmpty() ? "" : ": " + previous.getRemarks()),
                new java.util.Date()
        );
        ctx.adminActivityDAO.insertLog(undoLog);
        result.clearedUndoAction = previous;
        result.message = "Undid " + previous.getActionType().toLowerCase() + " on "
                + previous.getProductName() + " (back to " + restoredStock + ").";
        return true;
    }
}
