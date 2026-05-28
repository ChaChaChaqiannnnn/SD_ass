package com.shopease.strategy;

import com.shopease.dao.AdminActivityDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.model.AdminInventoryLog;
import com.shopease.model.User;
import com.shopease.observer.ShopEaseInventorySubject;

/**
 * Strategy Pattern — interchangeable admin inventory stock actions
 * (restock, reduce, undo).
 */
public interface ShopEaseInventoryActionStrategy {

    boolean execute(InventoryActionContext context, InventoryActionResult result);

    class InventoryActionContext {
        public final ProductDAO productDAO;
        public final AdminActivityDAO adminActivityDAO;
        public final ShopEaseInventorySubject inventorySubject;
        public final User currentUser;
        public final String productId;
        public final int amount;
        public final String remarks;
        public final AdminInventoryLog lastUndoableAction;

        public InventoryActionContext(ProductDAO productDAO,
                                      AdminActivityDAO adminActivityDAO,
                                      ShopEaseInventorySubject inventorySubject,
                                      User currentUser,
                                      String productId,
                                      int amount,
                                      String remarks,
                                      AdminInventoryLog lastUndoableAction) {
            this.productDAO = productDAO;
            this.adminActivityDAO = adminActivityDAO;
            this.inventorySubject = inventorySubject;
            this.currentUser = currentUser;
            this.productId = productId;
            this.amount = amount;
            this.remarks = remarks;
            this.lastUndoableAction = lastUndoableAction;
        }
    }

    class InventoryActionResult {
        public String message = "";
        public AdminInventoryLog newLog;
        public AdminInventoryLog clearedUndoAction;
    }
}
