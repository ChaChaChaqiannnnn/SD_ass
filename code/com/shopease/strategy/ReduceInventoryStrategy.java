package com.shopease.strategy;

/**
 * Strategy Pattern — reduces stock with mandatory remarks validation.
 */
public class ReduceInventoryStrategy implements ShopEaseInventoryActionStrategy {

    @Override
    public boolean execute(InventoryActionContext ctx, InventoryActionResult result) {
        if (ctx.remarks == null || ctx.remarks.trim().isEmpty()) {
            result.message = "Remarks are required when reducing stock.";
            return false;
        }
        if (ctx.amount <= 0) {
            result.message = "Reduce amount must be greater than 0.";
            return false;
        }
        return RestockInventoryStrategy.adjustStock(
                ctx, result, -ctx.amount, "REDUCE", ctx.remarks.trim());
    }
}
