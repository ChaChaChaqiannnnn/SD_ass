package com.shopease.strategy;

import com.shopease.dao.UserDAO;
import com.shopease.model.User;

/** Strategy Pattern — self-service customer registration algorithm. */
public interface ShopEaseRegisterCustomerStrategy {

    boolean register(UserDAO userDAO, User customer, RegisterCustomerResult result);

    class RegisterCustomerResult {
        public String message = "";
    }
}
