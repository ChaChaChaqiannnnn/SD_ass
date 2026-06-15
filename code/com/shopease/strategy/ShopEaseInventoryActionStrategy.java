package com.shopease.strategy;

import com.shopease.dao.AdminActivityDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.model.AdminInventoryLog;
import com.shopease.model.User;
import com.shopease.observer.ShopEaseInventorySubject;

/**
 * GoF Strategy — Strategy role (admin inventory algorithms).
 */
public interface ShopEaseInventoryActionStrategy {

    boolean algorithmInterface(InventoryActionRequest request, InventoryActionResult result);

    /** Input data passed into inventory strategy algorithms. */
    class InventoryActionRequest {
        public final ProductDAO productDAO;
        public final AdminActivityDAO adminActivityDAO;
        public final ShopEaseInventorySubject inventorySubject;
        public final User currentUser;
        public final String productId;
        public final int amount;
        public final String remarks;
        public final AdminInventoryLog lastUndoableAction;

        public InventoryActionRequest(ProductDAO productDAO,
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
