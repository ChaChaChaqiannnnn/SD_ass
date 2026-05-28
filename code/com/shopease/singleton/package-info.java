/**
 * Singleton Pattern — {@code com.shopease.singleton}
 * <p>
 * One shared instance per logged-in customer for in-memory session state, plus one app-wide database manager.
 * <ul>
 *   <li>{@link com.shopease.singleton.ShopEaseCartSingleton} — one cart per user ID</li>
 *   <li>{@link com.shopease.singleton.ShopEaseWishlistSingleton} — one wishlist session per user ID</li>
 *   <li>{@link com.shopease.singleton.ShopEaseDatabaseManager} — one SQLite connection manager for all DAOs</li>
 * </ul>
 * Persistence is delegated to DAOs; singletons hold the live session objects.
 */
package com.shopease.singleton;
