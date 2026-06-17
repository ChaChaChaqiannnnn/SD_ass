package com.shopease.strategy;

import com.shopease.model.Admin;
import com.shopease.model.AdminInventoryLog;
import com.shopease.model.Product;

/**
 * GoF Strategy — ConcreteStrategy (undo last inventory change).
 */
public class UndoInventoryStrategy implements ShopEaseInventoryActionStrategy {

    @Override
    public boolean algorithmInterface(InventoryActionRequest request, InventoryActionResult result) {
        if (!(request.currentUser instanceof Admin)) {
            result.message = "Nothing to undo.";
            return false;
        }
        if (request.lastUndoableAction == null) {
            result.message = "Nothing to undo.";
            return false;
        }

        AdminInventoryLog previous = request.lastUndoableAction;
        Product product = request.productDAO.getProductById(previous.getProductId());
        if (product == null) {
            result.message = "Product no longer exists.";
            result.clearedUndoAction = previous;
            return false;
        }

        int currentStock = product.getStockQuantity();
        int restoredStock = previous.getStockBefore();
        request.productDAO.updateStock(previous.getProductId(), restoredStock);
        request.inventorySubject.setStock(restoredStock, previous.getProductName());

        AdminInventoryLog undoLog = new AdminInventoryLog(
                request.currentUser.getUserId(),
                request.currentUser.getName(),
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
        request.adminActivityDAO.insertLog(undoLog);
        result.clearedUndoAction = previous;
        result.message = "Undid " + previous.getActionType().toLowerCase() + " on "
                + previous.getProductName() + " (back to " + restoredStock + ").";
        return true;
    }
}
