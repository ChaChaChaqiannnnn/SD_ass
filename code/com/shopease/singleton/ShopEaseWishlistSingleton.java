package com.shopease.singleton;

import com.shopease.dao.ProductDAO;
import com.shopease.dao.WishlistDAO;
import com.shopease.model.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One wishlist instance per customer (Singleton), persisted via WishlistDAO. */
public class ShopEaseWishlistSingleton {
    private static final Map<String, ShopEaseWishlistSingleton> instances = new HashMap<>();
    private final String userId;

    private ShopEaseWishlistSingleton(String userId) {
        this.userId = userId;
        System.out.println("[Memory] Wishlist instance for user: " + userId);
    }

    public static synchronized ShopEaseWishlistSingleton getInstance(String userId) {
        return instances.computeIfAbsent(userId, ShopEaseWishlistSingleton::new);
    }

    public String getUserId() {
        return userId;
    }

    public boolean add(WishlistDAO dao, String productId) {
        return dao.add(userId, productId);
    }

    public boolean remove(WishlistDAO dao, String productId) {
        return dao.remove(userId, productId);
    }

    public boolean contains(WishlistDAO dao, String productId) {
        return dao.contains(userId, productId);
    }

    public List<Product> getProducts(WishlistDAO dao, ProductDAO productDAO) {
        return dao.getProductsForUser(userId, productDAO);
    }
}
