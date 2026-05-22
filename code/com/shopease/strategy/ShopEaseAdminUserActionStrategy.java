package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;

/** Strategy for admin user-management actions. */
public interface ShopEaseAdminUserActionStrategy {
    boolean execute(UserDAO userDAO, User admin, String targetUserId, AdminUserActionResult result);

    class AdminUserActionResult {
        public String message = "";
        public String createdUserId;
    }
}
