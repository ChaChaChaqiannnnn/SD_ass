package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.Admin;
import com.shopease.model.User;

public class DeleteCustomerUserStrategy implements ShopEaseAdminUserActionStrategy {

    @Override
    public boolean execute(UserDAO userDAO, User admin, String targetUserId, AdminUserActionResult result) {
        if (!(admin instanceof Admin)) {
            result.message = "Only admins can manage users.";
            return false;
        }
        if (admin.getUserId().equals(targetUserId)) {
            result.message = "You cannot delete your own account.";
            return false;
        }
        User target = userDAO.getUserById(targetUserId);
        if (target == null) {
            result.message = "User not found.";
            return false;
        }
        if (target instanceof Admin) {
            result.message = "Cannot delete admin accounts.";
            return false;
        }
        if (!userDAO.deleteUser(targetUserId)) {
            result.message = "Could not delete user.";
            return false;
        }
        result.message = "User deleted.";
        return true;
    }
}
