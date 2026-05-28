package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;
import com.shopease.util.UserAccountUtils;

/**
 * Strategy Pattern — validates login credentials against the SQL database.
 */
public interface ShopEaseLoginValidationStrategy {

    boolean validate(UserDAO userDAO, String email, String password, LoginValidationResult result);

    class LoginValidationResult {
        public String message = "";
        public User user;
    }
}
