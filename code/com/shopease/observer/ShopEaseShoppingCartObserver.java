package com.shopease.observer;

import com.shopease.singleton.ShopEaseCartSingleton;
import com.shopease.model.CartItem;
import java.util.Iterator;

//this manages the user cart when items are sold out
public class ShopEaseShoppingCartObserver implements ShopEaseInventoryObserver {
    private ShopEaseCartSingleton cart;

    public ShopEaseShoppingCartObserver(ShopEaseCartSingleton cart) {
        this.cart = cart;
    }

    @Override
    public void update(String event, String productName) {
        if (event.equals("OUT_OF_STOCK")) {
            //this shows a message that the item is gone
            System.out.println("\n[OBSERVER] SYSTEM ALERT: " + productName + " is now out of stock!");

            Iterator<CartItem> iterator = cart.getItems().iterator();
            while (iterator.hasNext()) {
                CartItem item = iterator.next();
                if (item.getProduct().getName().equalsIgnoreCase(productName)) {
                    //this removes the item so the user cannot buy it
                    iterator.remove();
                    //this tells the user we took it out of their cart
                    System.out.println("[OBSERVER] Auto-removed '" + productName + "' from your cart to prevent checkout errors.");
                }
            }
        }
    }
}
