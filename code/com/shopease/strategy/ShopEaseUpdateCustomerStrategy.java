package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;

/** Strategy Pattern — admin update-customer algorithm. */
public interface ShopEaseUpdateCustomerStrategy {
    boolean update(UserDAO userDAO, User admin, String userId, String name, String email, String password,
                   ShopEaseAdminUserActionStrategy.AdminUserActionResult result);
}
