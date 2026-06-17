package com.shopease.strategy;

/**
 * GoF Strategy — ConcreteStrategy (reduce inventory).
 */
public class ReduceInventoryStrategy implements ShopEaseInventoryActionStrategy {

    @Override
    public boolean algorithmInterface(InventoryActionRequest request, InventoryActionResult result) {
        if (request.remarks == null || request.remarks.trim().isEmpty()) {
            result.message = "Remarks are required when reducing stock.";
            return false;
        }
        if (request.amount <= 0) {
            result.message = "Reduce amount must be greater than 0.";
            return false;
        }
        return RestockInventoryStrategy.adjustStock(
                request, result, -request.amount, "REDUCE", request.remarks.trim());
    }
}
