package com.shopease.strategy;

/**
 * GoF Strategy — Context role (admin inventory actions).
 * Holds a {@link ShopEaseInventoryActionStrategy} and delegates via
 * {@link #contextInterface(ShopEaseInventoryActionStrategy.InventoryActionRequest, ShopEaseInventoryActionStrategy.InventoryActionResult)}.
 */
public class ShopEaseInventoryActionContext {
    private ShopEaseInventoryActionStrategy strategy;

    public ShopEaseInventoryActionContext(ShopEaseInventoryActionStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ShopEaseInventoryActionStrategy strategy) {
        this.strategy = strategy;
    }

    /** GoF — delegates to strategy.algorithmInterface(). */
    public boolean contextInterface(ShopEaseInventoryActionStrategy.InventoryActionRequest request,
                                    ShopEaseInventoryActionStrategy.InventoryActionResult result) {
        return strategy.algorithmInterface(request, result);
    }
}
