/**
 * Strategy Pattern — {@code com.shopease.strategy}
 * <p>
 * Encapsulates interchangeable algorithms selected at runtime.
 * <ul>
 *   <li><b>Payment:</b> {@link com.shopease.strategy.ShopEasePaymentStrategy} + CreditCard / DuitNow / MAE / TNG</li>
 *   <li><b>Payment context:</b> {@link com.shopease.strategy.ShopEasePaymentContext}</li>
 *   <li><b>Payment selector:</b> {@link com.shopease.strategy.ShopEasePaymentStrategySelector}</li>
 *   <li><b>Profile update:</b> {@link com.shopease.strategy.ShopEaseProfileUpdateStrategy} + {@link com.shopease.strategy.CustomerProfileUpdateStrategy}</li>
 *   <li><b>Admin user CRUD:</b> {@link com.shopease.strategy.ShopEaseCreateCustomerStrategy},
 *       {@link com.shopease.strategy.ShopEaseUpdateCustomerStrategy},
 *       {@link com.shopease.strategy.ShopEaseAdminUserActionStrategy} + DeleteCustomerUserStrategy</li>
 *   <li><b>Self registration:</b> {@link com.shopease.strategy.ShopEaseRegisterCustomerStrategy}
 *       + {@link com.shopease.strategy.RegisterCustomerStrategy}</li>
 *   <li><b>Login validation:</b> {@link com.shopease.strategy.ShopEaseLoginValidationStrategy}
 *       + {@link com.shopease.strategy.DefaultLoginValidationStrategy}</li>
 *   <li><b>Admin inventory:</b> {@link com.shopease.strategy.ShopEaseInventoryActionStrategy}
 *       + RestockInventoryStrategy, ReduceInventoryStrategy, UndoInventoryStrategy</li>
 * </ul>
 */
package com.shopease.strategy;
