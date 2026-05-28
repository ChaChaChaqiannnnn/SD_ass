package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;

/**
 * Strategy Pattern — interface for admin actions on customer accounts (e.g. delete).
 * Create and Update have their own interfaces; Delete uses this one.
 */
public interface ShopEaseAdminUserActionStrategy {
    boolean execute(UserDAO userDAO, User admin, String targetUserId, AdminUserActionResult result);

    class AdminUserActionResult {
        public String message = "";
        public String createdUserId;
    }
}
