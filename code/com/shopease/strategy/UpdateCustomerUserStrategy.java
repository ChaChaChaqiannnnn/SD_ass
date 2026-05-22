package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.Admin;
import com.shopease.model.Customer;
import com.shopease.model.User;

public class UpdateCustomerUserStrategy {

    public boolean update(UserDAO userDAO, User admin, String userId, String name, String email, String password,
                          ShopEaseAdminUserActionStrategy.AdminUserActionResult result) {
        if (!(admin instanceof Admin)) {
            result.message = "Only admins can update users.";
            return false;
        }
        User target = userDAO.getUserById(userId);
        if (target == null) {
            result.message = "User not found.";
            return false;
        }
        if (target instanceof Admin) {
            result.message = "Cannot edit admin accounts here.";
            return false;
        }
        if (name == null || name.trim().isEmpty()) {
            result.message = "Name is required.";
            return false;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            result.message = "Valid email is required.";
            return false;
        }
        String passwordToSave = password == null ? "" : password;
        if (passwordToSave.isEmpty()) {
            passwordToSave = target.getPassword();
        } else if (passwordToSave.length() < 4) {
            result.message = "Password must be at least 4 characters.";
            return false;
        }
        String trimmedEmail = email.trim();
        if (userDAO.emailUsedByOtherUser(trimmedEmail, userId)) {
            result.message = "Email is already used by another account.";
            return false;
        }
        if (!userDAO.updateUserProfile(userId, name.trim(), trimmedEmail, passwordToSave)) {
            result.message = "Could not update customer.";
            return false;
        }
        result.message = "Customer updated.";
        return true;
    }
}
