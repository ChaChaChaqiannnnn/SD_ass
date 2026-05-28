/**
 * Observer Pattern — {@code com.shopease.observer}
 * <p>
 * Subject notifies observers when inventory, cart, or UI data changes.
 * <ul>
 *   <li>{@link com.shopease.observer.Observer} — base observer contract</li>
 *   <li>{@link com.shopease.observer.ShopEaseSubject} — subject contract (attach / detach / notify)</li>
 *   <li>{@link com.shopease.observer.ShopEaseInventorySubject} — concrete subject (stock + app events)</li>
 *   <li>{@link com.shopease.observer.ShopEaseInventoryObserver} — app-wide observer interface</li>
 * </ul>
 * <b>Function observers (naming: XxxFunctionObserver):</b>
 * <ul>
 *   <li>{@link com.shopease.observer.ShopEaseInventoryAdminLogObserver} — console log for admin on LOW/OUT stock</li>
 *   <li>{@link com.shopease.observer.ShopEaseInventoryAdminAlertObserver} — admin GUI popup on stock alerts</li>
 *   <li>{@link com.shopease.observer.ShopEaseAdminLoginStockAlertObserver} — admin login low-stock summary popup</li>
 *   <li>{@link com.shopease.observer.ShopEaseCustomerCartReminderObserver} — customer login cart reminder popup</li>
 *   <li>{@link com.shopease.observer.ShopEaseCustomerWishlistRestockObserver} — customer wishlist restock popup</li>
 *   <li>{@link com.shopease.observer.ShopEaseDataChangeRefreshObserver} — UI refresh on DATA_CHANGED</li>
 *   <li>{@link com.shopease.observer.ShopEaseCartStockSyncObserver} — sync cart when stock changes</li>
 *   <li>{@link com.shopease.observer.ShopEaseAppEvents} — shared event name constants</li>
 * </ul>
 */
package com.shopease.observer;
