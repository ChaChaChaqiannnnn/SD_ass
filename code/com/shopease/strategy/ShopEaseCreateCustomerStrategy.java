package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;

/** Strategy Pattern — admin create-customer algorithm. */
public interface ShopEaseCreateCustomerStrategy {
    boolean create(UserDAO userDAO, User admin, String name, String email, String password,
                   ShopEaseAdminUserActionStrategy.AdminUserActionResult result);
}
