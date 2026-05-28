package com.shopease.singleton;

import com.shopease.model.CartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton Pattern — one shopping cart per customer user ID.
 * <p>
 * Explain it like this: "If the same customer logs in twice, they get the SAME cart object
 * from memory — we don't create a new empty cart every time."
 */
public class ShopEaseCartSingleton {
    // Map of userId → their one cart instance (this is the Singleton registry)
    private static Map<String, ShopEaseCartSingleton> instances = new HashMap<>();
    private String userId;
    private List<CartItem> items;

    // Private constructor — outside code MUST call getInstance() instead of "new"
    private ShopEaseCartSingleton(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        System.out.println("[Memory] New Cart instance created for user: " + userId);
    }

    /** Returns the existing cart for this user, or creates one if it's their first login. */
    public static synchronized ShopEaseCartSingleton getInstance(String userId) {
        if (!instances.containsKey(userId)) {
            //this makes a brand new cart if it is not there
            instances.put(userId, new ShopEaseCartSingleton(userId));
        } else {
            //this finds the old cart in the memory
            System.out.println("[Memory] Returning existing cart instance for user: " + userId);
        }
        return instances.get(userId);
    }

    //this puts a new item into the user cart
    public void addItem(CartItem item) {
        this.items.add(item);
    }

    //this shows everything that is inside the cart
    public List<CartItem> getItems() {
        return items;
    }

    //this tells us which user owns this cart
    public String getUserId() {
        return userId;
    }
}
