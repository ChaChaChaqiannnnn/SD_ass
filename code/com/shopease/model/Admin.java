package com.shopease.model;

public class Admin extends User {
    public Admin(String userId, String name, String email, String password) {
        super(userId, name, email, password, "Admin");
    }
}
