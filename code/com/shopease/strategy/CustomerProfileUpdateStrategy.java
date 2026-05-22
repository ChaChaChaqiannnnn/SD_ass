package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.Customer;
import com.shopease.model.User;

public class CustomerProfileUpdateStrategy implements ShopEaseProfileUpdateStrategy {

    @Override
    public boolean update(UserDAO userDAO, User currentUser, String name, String email, String newPassword,
                          ProfileUpdateResult result) {
        if (!(currentUser instanceof Customer)) {
            result.message = "Only customers can update this profile.";
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
        String passwordToSave = newPassword == null ? "" : newPassword;
        if (passwordToSave.isEmpty()) {
            passwordToSave = currentUser.getPassword();
        } else if (passwordToSave.length() < 4) {
            result.message = "Password must be at least 4 characters.";
            return false;
        }
        String trimmedEmail = email.trim();
        if (userDAO.emailUsedByOtherUser(trimmedEmail, currentUser.getUserId())) {
            result.message = "Email is already used by another account.";
            return false;
        }
        if (!userDAO.updateUserProfile(currentUser.getUserId(), name.trim(), trimmedEmail, passwordToSave)) {
            result.message = "Could not update profile.";
            return false;
        }
        result.updatedUser = userDAO.getUserById(currentUser.getUserId());
        result.message = "Profile updated successfully.";
        return true;
    }
}
