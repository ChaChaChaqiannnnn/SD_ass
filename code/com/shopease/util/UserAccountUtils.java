package com.shopease.util;

/** Shared validation and normalization for user accounts. */
public final class UserAccountUtils {
    private UserAccountUtils() {}

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    public static boolean isValidEmail(String email) {
        String n = normalizeEmail(email);
        return !n.isEmpty() && n.contains("@") && n.indexOf('@') > 0 && n.indexOf('@') < n.length() - 1;
    }
}
