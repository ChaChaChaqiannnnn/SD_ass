package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.Customer;
import com.shopease.model.User;

/** Strategy for customer profile updates. */
public interface ShopEaseProfileUpdateStrategy {
    boolean update(UserDAO userDAO, User currentUser, String name, String email, String newPassword,
                   ProfileUpdateResult result);

    class ProfileUpdateResult {
        public String message = "";
        public User updatedUser;
    }
}
