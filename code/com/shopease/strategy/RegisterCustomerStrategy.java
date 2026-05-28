package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.Customer;
import com.shopease.model.User;
import com.shopease.util.CustomerIdGenerator;
import com.shopease.util.UserAccountUtils;

/**
 * Strategy Pattern — customer self-registration from the sign-up screen.
 * Keeps validation and DAO writes out of {@code SignUpPanel}.
 */
public class RegisterCustomerStrategy implements ShopEaseRegisterCustomerStrategy {

    @Override
    public boolean register(UserDAO userDAO, User customer, RegisterCustomerResult result) {
        if (customer == null) {
            result.message = "Invalid registration data.";
            return false;
        }
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            result.message = "Name is required.";
            return false;
        }
        String email = UserAccountUtils.normalizeEmail(customer.getEmail());
        if (!UserAccountUtils.isValidEmail(email)) {
            result.message = "Valid email is required.";
            return false;
        }
        if (customer.getPassword() == null || customer.getPassword().length() < 4) {
            result.message = "Password must be at least 4 characters.";
            return false;
        }
        String userId = customer.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = CustomerIdGenerator.nextId();
        }
        if (userDAO.getUserByEmail(email) != null) {
            result.message = "An account with this email already exists.";
            return false;
        }
        if (userDAO.getUserById(userId) != null) {
            userId = CustomerIdGenerator.nextId();
        }
        Customer toSave = new Customer(userId, customer.getName().trim(), email, customer.getPassword());
        if (!userDAO.insertUser(toSave)) {
            String kind = userDAO.lastInsertErrorKind(toSave);
            if ("email".equals(kind)) {
                result.message = "An account with this email already exists.";
            } else if ("id".equals(kind)) {
                result.message = "Could not assign a unique user ID. Please try again.";
            } else {
                result.message = "Registration failed. Please try again.";
            }
            return false;
        }
        result.message = "Account created successfully.";
        return true;
    }
}
