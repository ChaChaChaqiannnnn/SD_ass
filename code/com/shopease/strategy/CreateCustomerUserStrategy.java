package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.Admin;
import com.shopease.model.Customer;
import com.shopease.model.User;
import com.shopease.util.CustomerIdGenerator;
import com.shopease.util.UserAccountUtils;

/**
 * Strategy Pattern — admin creates a new customer account.
 * Keeps validation logic out of the UI panel.
 */
public class CreateCustomerUserStrategy implements ShopEaseCreateCustomerStrategy {

    @Override
    public boolean create(UserDAO userDAO, User admin, String name, String email, String password,
                            ShopEaseAdminUserActionStrategy.AdminUserActionResult result) {
        if (!(admin instanceof Admin)) {
            result.message = "Only admins can create users.";
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
        if (password == null || password.length() < 4) {
            result.message = "Password must be at least 4 characters.";
            return false;
        }
        String trimmedEmail = UserAccountUtils.normalizeEmail(email);
        if (!UserAccountUtils.isValidEmail(trimmedEmail)) {
            result.message = "Valid email is required.";
            return false;
        }
        if (userDAO.getUserByEmail(trimmedEmail) != null) {
            result.message = "Email is already registered.";
            return false;
        }
        String userId = CustomerIdGenerator.nextId();
        Customer customer = new Customer(userId, name.trim(), trimmedEmail, password);
        if (!userDAO.insertUser(customer)) {
            result.message = "Could not create customer.";
            return false;
        }
        result.message = "Customer created: " + userId;
        result.createdUserId = userId;
        return true;
    }
}
