package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;
import com.shopease.util.UserAccountUtils;

/**
 * Strategy Pattern — default email/password login validation for ShopEase.
 */
public class DefaultLoginValidationStrategy implements ShopEaseLoginValidationStrategy {

    @Override
    public boolean validate(UserDAO userDAO, String email, String password, LoginValidationResult result) {
        if (email == null || password == null) {
            result.message = "Email and password are required.";
            return false;
        }
        String trimmedEmail = UserAccountUtils.normalizeEmail(email);
        User user = userDAO.getUserByEmail(trimmedEmail);
        if (user != null && user.getPassword().equals(password)) {
            result.user = user;
            return true;
        }
        result.message = "Invalid email or password.";
        return false;
    }
}
