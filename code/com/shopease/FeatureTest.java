package com.shopease;

import com.shopease.model.Admin;
import com.shopease.model.Customer;
import com.shopease.model.InventoryStockStatus;
import com.shopease.model.Product;
import com.shopease.model.User;
import com.shopease.observer.ShopEaseInventorySubject;
import com.shopease.service.ShopEaseService;
import com.shopease.strategy.ShopEaseCreditCardStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Automated checks for assignment features (no GUI).
 * Run: java -cp bin:lib/sqlite-jdbc.jar com.shopease.FeatureTest
 */
public class FeatureTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== ShopEase Feature Test Suite ===\n");
        ensureProductStock("MSE-02", 10);
        ensureProductStock("LPT-01", 25);
        ensureProductStock("KBD-03", 10);

        testInventoryStockStatusLabels();
        testWishlist();
        testCartReminder();
        testAdminInventoryNotifications();
        testAdminStockColorsData();
        testProfileUpdate();
        testOrderIdFormat();
        testWishlistRestockOnLogin();
        testManageUsers();

        System.out.println("\n=== Results: " + passed + " passed, " + failed + " failed ===");
        if (failed > 0) {
            System.exit(1);
        }
        System.out.println("ALL FEATURE TESTS PASSED");
    }

    // --- 4) Stock status labels (GREEN / YELLOW / RED logic) ---
    private static void testInventoryStockStatusLabels() {
        System.out.println("[4] Admin stock status colours (logic)");
        assertEquals("NO STOCK", InventoryStockStatus.label(0), "label 0 stock");
        assertEquals("LOW STOCK", InventoryStockStatus.label(4), "label 4 stock");
        assertEquals("OK", InventoryStockStatus.label(5), "label 5 stock");
        assertEquals("OK", InventoryStockStatus.label(100), "label 100 stock");
        assertTrue(InventoryStockStatus.isOutOfStock(0), "out of stock");
        assertTrue(InventoryStockStatus.isLowStock(3), "low stock");
        assertTrue(InventoryStockStatus.isOk(5), "ok stock");
        pass("Stock status thresholds OK");
    }

    // --- 1) Wishlist ---
    private static void testWishlist() throws Exception {
        System.out.println("[1] Wishlist");
        ShopEaseService s = new ShopEaseService();
        long ts = System.currentTimeMillis();
        String email = "wish" + ts + "@test.my";
        Customer c = new Customer("CUST-W-" + ts, "Wish Tester", email, "pass");
        assertTrue(s.registerCustomer(c), "register: " + s.getLastMessage());
        assertTrue(s.login(email, "pass"), "login");

        Product mouse = s.getProductById("MSE-02");
        assertNotNull(mouse, "mouse product");

        assertTrue(s.addToWishlist(mouse.getProductId()), "add wishlist: " + s.getLastMessage());
        assertTrue(s.isInWishlist(mouse.getProductId()), "in wishlist");
        assertEquals(1, s.getWishlistProducts().size(), "wishlist size 1");

        assertFalse(s.addToWishlist(mouse.getProductId()), "duplicate wishlist blocked");
        assertTrue(s.removeFromWishlist(mouse.getProductId()), "remove wishlist");
        assertFalse(s.isInWishlist(mouse.getProductId()), "not in wishlist after remove");

        assertTrue(s.addToWishlist(mouse.getProductId()), "re-add wishlist");
        assertTrue(s.moveWishlistItemToCart(mouse.getProductId(), 1), "move to cart: " + s.getLastMessage());
        assertFalse(s.isInWishlist(mouse.getProductId()), "removed from wishlist after move");
        assertEquals(1, s.getCartItemCount(), "cart has 1 item");

        assertFalse(s.addToWishlist("FAKE-ID"), "fake product wishlist");

        s.logout();
        pass("Wishlist add/remove/move/cart OK");
    }

    // --- 3b) Customer cart reminder message ---
    private static void testCartReminder() throws Exception {
        System.out.println("[3] Cart reminder (customer)");
        ShopEaseService s = new ShopEaseService();
        long ts = System.currentTimeMillis();
        String email = "cart" + ts + "@test.my";
        s.registerCustomer(new Customer("CUST-C-" + ts, "Cart User", email, "pass"));
        assertTrue(s.login(email, "pass"), "login");

        List<String> cartMessages = new ArrayList<>();
        s.attachObserver((event, detail) -> {
            if ("CART_REMINDER".equals(event)) {
                cartMessages.add(detail);
            }
        });

        s.publishCartReminderIfNeeded();
        assertEquals(0, cartMessages.size(), "no reminder empty cart");

        Product mouse = s.getProductById("MSE-02");
        assertTrue(s.addToCart(mouse, 1), "add to cart");
        s.publishCartReminderIfNeeded();
        assertEquals(1, cartMessages.size(), "reminder when cart has items");
        assertTrue(cartMessages.get(0).contains("1 item"), "singular: " + cartMessages.get(0));

        assertTrue(s.addToCart(mouse, 1), "add second");
        cartMessages.clear();
        s.publishCartReminderIfNeeded();
        assertTrue(cartMessages.get(0).contains("2 item"), "plural: " + cartMessages.get(0));

        pass("Cart reminder messages OK");
    }

    // --- 3a) Admin low-stock notification listener ---
    private static void testAdminInventoryNotifications() throws Exception {
        System.out.println("[3] Admin stock notifications (listener)");
        ShopEaseService s = new ShopEaseService();
        List<String> events = new ArrayList<>();
        List<String> products = new ArrayList<>();

        assertTrue(s.login("admin@email.admin.my", "adminpass"), "admin login");
        s.attachObserver((event, name) -> {
            events.add(event);
            products.add(name);
        });

        Product kbd = s.getProductById("KBD-03");
        int before = kbd.getStockQuantity();
        if (before < 6) {
            s.restockProduct(kbd.getProductId(), 10 - before + 6);
            kbd = s.getProductById("KBD-03");
            before = kbd.getStockQuantity();
        }

        events.clear();
        assertTrue(s.reduceStockProduct(kbd.getProductId(), before - 2, "audit test"), "reduce: " + s.getLastMessage());
        Thread.sleep(150);
        assertTrue(events.contains("LOW_STOCK"), "LOW_STOCK event fired, got: " + events);

        events.clear();
        assertTrue(s.reduceStockProduct(kbd.getProductId(), 2, "clear shelf"), "reduce to zero");
        Thread.sleep(150);
        assertTrue(events.contains("OUT_OF_STOCK"), "OUT_OF_STOCK event fired, got: " + events);

        ShopEaseInventorySubject subject = new ShopEaseInventorySubject();
        AtomicInteger calls = new AtomicInteger();
        subject.attach((e, p) -> calls.incrementAndGet());
        subject.setStock(0, "TestProd");
        assertEquals(1, calls.get(), "observer OUT_OF_STOCK");
        subject.setStock(3, "TestProd");
        assertEquals(2, calls.get(), "observer LOW_STOCK");

        pass("Inventory notifications OK");
    }

    // --- 4b) Stock values after admin ops (data layer) ---
    private static void testAdminStockColorsData() {
        System.out.println("[4] Admin stock data after restock/reduce");
        ShopEaseService s = new ShopEaseService();
        assertTrue(s.login("admin@email.admin.my", "adminpass"), "admin login");

        Product laptop = s.getProductById("LPT-01");
        s.restockProduct(laptop.getProductId(), 20);
        laptop = s.getProductById("LPT-01");
        assertTrue(InventoryStockStatus.isOk(laptop.getStockQuantity()), "restock -> OK");

        s.reduceStockProduct(laptop.getProductId(), laptop.getStockQuantity() - 2, "demo low");
        laptop = s.getProductById("LPT-01");
        assertTrue(InventoryStockStatus.isLowStock(laptop.getStockQuantity()), "reduce -> LOW");

        s.reduceStockProduct(laptop.getProductId(), 2, "demo out");
        laptop = s.getProductById("LPT-01");
        assertTrue(InventoryStockStatus.isOutOfStock(laptop.getStockQuantity()), "reduce -> OUT");

        pass("Admin stock levels OK/LOW/OUT in database");
    }

    // --- 5) Profile ---
    private static void testProfileUpdate() {
        System.out.println("[5] Customer profile");
        ShopEaseService s = new ShopEaseService();
        long ts = System.currentTimeMillis();
        String email = "prof" + ts + "@test.my";
        String newEmail = "prof2" + ts + "@test.my";
        s.registerCustomer(new Customer("CUST-P-" + ts, "Old Name", email, "oldpass"));
        assertTrue(s.login(email, "oldpass"), "login");

        assertFalse(s.updateCustomerProfile("", email, "newpass"), "empty name rejected");
        assertFalse(s.updateCustomerProfile("New Name", "bademail", "newpass"), "bad email rejected");
        assertFalse(s.updateCustomerProfile("New Name", newEmail, "ab"), "short password rejected");

        assertTrue(s.updateCustomerProfile("New Name", newEmail, "newpass"), "update: " + s.getLastMessage());
        assertEquals("New Name", s.getCurrentUser().getName(), "name updated");
        s.logout();

        assertTrue(s.login(newEmail, "newpass"), "login with new credentials");
        assertTrue(s.updateCustomerProfile("New Name Only", newEmail, ""), "name only empty password");
        assertEquals("New Name Only", s.getCurrentUser().getName(), "name only update");
        s.logout();

        assertTrue(s.login(newEmail, "newpass"), "password unchanged after blank");
        assertEquals("New Name Only", s.getCurrentUser().getName(), "name persisted");

        assertFalse(s.updateCustomerProfile("X", "admin@email.admin.my", "x"), "admin email taken");

        pass("Profile update OK");
    }

    // --- Order ID: dd/MM/yy-NNN ---
    private static void testOrderIdFormat() {
        System.out.println("[Order] Order ID format");
        ShopEaseService s = new ShopEaseService();
        long ts = System.currentTimeMillis();
        String email = "ord" + ts + "@test.my";
        s.registerCustomer(new Customer("CUST-O-" + ts, "Order Test", email, "pass"));
        assertTrue(s.login(email, "pass"), "login");
        ensureProductStock("MSE-02", 5);
        Product mouse = s.getProductById("MSE-02");
        assertTrue(s.addToCart(mouse, 1), "add to cart");
        assertTrue(s.checkout(new ShopEaseCreditCardStrategy()), "checkout: " + s.getLastMessage());
        String orderId = s.getOrderHistory().get(0).getOrderId();
        assertTrue(orderId.matches("\\d{2}/\\d{2}/\\d{2}-\\d{3}"),
                "order id dd/MM/yy-NNN: " + orderId);
        pass("Order ID format OK: " + orderId);
    }

    // --- Wishlist restock on login (Observer) ---
    private static void testWishlistRestockOnLogin() throws Exception {
        System.out.println("[Wishlist] Restock notification on login");
        long ts = System.currentTimeMillis();
        String email = "restock" + ts + "@test.my";
        String userId = "CUST-R-" + ts;

        ShopEaseService admin = new ShopEaseService();
        assertTrue(admin.login("admin@email.admin.my", "adminpass"), "admin login");
        Product mouse = admin.getProductById("MSE-02");
        if (mouse.getStockQuantity() > 0) {
            admin.reduceStockProduct("MSE-02", mouse.getStockQuantity(), "test zero");
        }
        admin.logout();

        ShopEaseService customer = new ShopEaseService();
        customer.registerCustomer(new Customer(userId, "Restock User", email, "pass"));
        assertTrue(customer.login(email, "pass"), "customer login");
        assertTrue(customer.addToWishlist("MSE-02"), "add wishlist");
        customer.logout();

        admin = new ShopEaseService();
        assertTrue(admin.login("admin@email.admin.my", "adminpass"), "admin restock");
        admin.restockProduct("MSE-02", 5);
        admin.logout();

        List<String> restockMsgs = new ArrayList<>();
        customer = new ShopEaseService();
        assertTrue(customer.login(email, "pass"), "customer re-login");
        customer.attachObserver((event, detail) -> {
            if ("WISHLIST_RESTOCK".equals(event)) {
                restockMsgs.add(detail);
            }
        });
        customer.publishWishlistRestockOnLogin();
        Thread.sleep(50);
        assertEquals(1, restockMsgs.size(), "restock event");
        assertTrue(restockMsgs.get(0).contains("Wireless Mouse"), "product in message");
        pass("Wishlist restock on login OK");
    }

    // --- 2) Manage users ---
    private static void testManageUsers() {
        System.out.println("[2] Manage users (admin)");
        ShopEaseService s = new ShopEaseService();
        long ts = System.currentTimeMillis();
        String victimEmail = "del" + ts + "@test.my";
        String victimId = "CUST-DEL-" + ts;
        ShopEaseService s2 = new ShopEaseService();
        s2.registerCustomer(new Customer(victimId, "To Delete", victimEmail, "pass"));

        assertTrue(s.login("admin@email.admin.my", "adminpass"), "admin login");
        List<User> users = s.getAllUsersForAdmin();
        assertTrue(users.stream().anyMatch(u -> victimId.equals(u.getUserId())), "victim listed");

        assertFalse(s.deleteUserAsAdmin("ADMIN-001"), "cannot delete self");
        assertFalse(s.deleteUserAsAdmin("NO-SUCH"), "missing user");

        assertTrue(s.deleteUserAsAdmin(victimId), "delete: " + s.getLastMessage());
        users = s.getAllUsersForAdmin();
        assertFalse(users.stream().anyMatch(u -> victimId.equals(u.getUserId())), "victim removed");

        s.logout();
        ShopEaseService s3 = new ShopEaseService();
        assertFalse(s3.login(victimEmail, "pass"), "deleted cannot login");

        pass("Manage users OK");
    }

    /** Restocks test products so repeated test runs do not fail on depleted inventory. */
    private static void ensureProductStock(String productId, int minimum) {
        ShopEaseService s = new ShopEaseService();
        assertTrue(s.login("admin@email.admin.my", "adminpass"), "admin login for stock setup");
        Product p = s.getProductById(productId);
        if (p != null && p.getStockQuantity() < minimum) {
            s.restockProduct(productId, minimum - p.getStockQuantity());
        }
        s.logout();
    }

    private static void pass(String msg) {
        passed++;
        System.out.println("  PASS: " + msg);
    }

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) {
            failed++;
            System.err.println("  FAIL: " + msg);
        }
    }

    private static void assertFalse(boolean cond, String msg) {
        assertTrue(!cond, msg);
    }

    private static void assertNull(Object o, String msg) {
        assertTrue(o == null, msg);
    }

    private static void assertNotNull(Object o, String msg) {
        assertTrue(o != null, msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        assertTrue(expected == null ? actual == null : expected.equals(actual),
                msg + " (expected=" + expected + ", actual=" + actual + ")");
    }

    private static void assertEquals(int expected, int actual, String msg) {
        assertTrue(expected == actual, msg + " (expected=" + expected + ", actual=" + actual + ")");
    }
}
