package com.shopease.singleton;

import com.shopease.model.CartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GoF Singleton (extended) — one shopping cart instance per customer user ID.
 * Classic GoF uses a single {@code instance}; here a registry holds one cart per user.
 */
public class ShopEaseCartSingleton {
    private static final Map<String, ShopEaseCartSingleton> instances = new HashMap<>();
    private final String userId;
    private final List<CartItem> items;

    private ShopEaseCartSingleton(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        System.out.println("[Memory] New Cart instance created for user: " + userId);
    }

    public static synchronized ShopEaseCartSingleton getInstance(String userId) {
        if (!instances.containsKey(userId)) {
            instances.put(userId, new ShopEaseCartSingleton(userId));
        } else {
            System.out.println("[Memory] Returning existing cart instance for user: " + userId);
        }
        return instances.get(userId);
    }

    public void addItem(CartItem item) {
        this.items.add(item);
    }

    public List<CartItem> getItems() {
        return items;
    }

    public String getUserId() {
        return userId;
    }
}
