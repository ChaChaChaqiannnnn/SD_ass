package com.shopease.singleton;

import com.shopease.model.CartItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//this class makes sure every user has only one cart
public class ShopEaseCartSingleton {
    //this stores all the carts for different users
    private static Map<String, ShopEaseCartSingleton> instances = new HashMap<>();
    private String userId;
    private List<CartItem> items;

    //this creates a new cart for a specific user
    private ShopEaseCartSingleton(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        System.out.println("[Memory] New Cart instance created for user: " + userId);
    }

    //this gives back the cart for the user or makes a new one
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
