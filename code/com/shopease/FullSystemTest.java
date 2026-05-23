package com.shopease;

import com.shopease.model.*;
import com.shopease.observer.ShopEaseInventorySubject;
import com.shopease.service.ShopEaseService;
import com.shopease.singleton.ShopEaseCartSingleton;
import com.shopease.strategy.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ShopEase Full System Test Suite
 * ================================
 * Covers every testable functional feature of the ShopEase E-Commerce System.
 *
 * Run: java -cp "bin:lib/sqlite-jdbc.jar" com.shopease.FullSystemTest
 */
public class FullSystemTest {

    // ─── Test counters ──────────────────────────────────────────────────────────
    private static int passed  = 0;
    private static int failed  = 0;
    private static final List<String> failures = new ArrayList<>();

    // ─── Entry point ────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        printBanner("ShopEase Full System Test Suite");

        // Ensure all seed products have usable stock before any test runs
        ensureStock("MSE-02", 30);
        ensureStock("LPT-01", 30);
        ensureStock("KBD-03", 30);

        // ── Module 1 : Authentication ──────────────────────────────────────────
        testSection("MODULE 1 — AUTHENTICATION");
        testLoginSuccess();
        testLoginWrongPassword();
        testLoginNonexistentEmail();
        testLoginNullInputs();
        testLoginEmailCaseInsensitive();
        testLoginTrimsWhitespace();
        testLogout();

        // ── Module 2 : Registration ────────────────────────────────────────────
        testSection("MODULE 2 — REGISTRATION");
        testRegisterSuccess();
        testRegisterDuplicateEmail();
        testRegisterNullCustomer();
        testRegisterEmptyName();
        testRegisterInvalidEmail();
        testRegisterShortPassword();
        testRegisterEmailCaseInsensitive();

        // ── Module 3 : Product Catalogue ──────────────────────────────────────
        testSection("MODULE 3 — PRODUCT CATALOGUE");
        testGetAllProducts();
        testGetProductById();
        testGetProductByInvalidId();

        // ── Module 4 : Shopping Cart ───────────────────────────────────────────
        testSection("MODULE 4 — SHOPPING CART");
        testAddToCartBasic();
        testAddToCartAccumulates();
        testAddToCartExceedsStock();
        testAddToCartZeroQty();
        testAddToCartNegativeQty();
        testAddToCartNullProduct();
        testAddToCartOutOfStock();
        testAdminCannotUseCart();
        testRemoveFromCart();
        testRemoveNonexistentFromCart();
        testUpdateCartQty();
        testUpdateCartQtyToZeroRemovesItem();
        testUpdateCartQtyExceedsStock();
        testClearCart();
        testCartTotal();
        testGetCartItemCount();
        testCartReminderMessageSingular();
        testCartReminderMessagePlural();
        testCartReminderEmpty();
        testSyncCartRemovesOutOfStockItem();

        // ── Module 5 : Checkout & Orders ──────────────────────────────────────
        testSection("MODULE 5 — CHECKOUT & ORDERS");
        testCheckoutCreditCard();
        testCheckoutDuitNow();
        testCheckoutMAE();
        testCheckoutTNG();
        testCheckoutEmptyCart();
        testCheckoutDeductsStock();
        testCheckoutClearsCart();
        testOrderIdFormat();
        testOrderHistory();
        testAdminViewCustomerOrders();
        testAdminViewCustomerOrdersNotAdmin();

        // ── Module 6 : Wishlist ────────────────────────────────────────────────
        testSection("MODULE 6 — WISHLIST");
        testWishlistAdd();
        testWishlistDuplicate();
        testWishlistRemove();
        testWishlistIsInWishlist();
        testWishlistFakeProduct();
        testWishlistAdminBlocked();
        testMoveWishlistItemToCart();
        testMoveWishlistFakeProduct();
        testGetWishlistProducts();
        testWishlistRestockNotification();

        // ── Module 7 : Customer Profile ───────────────────────────────────────
        testSection("MODULE 7 — CUSTOMER PROFILE");
        testProfileUpdateAllFields();
        testProfileUpdateNameOnly();
        testProfileUpdateRejectsEmptyName();
        testProfileUpdateRejectsBadEmail();
        testProfileUpdateRejectsShortPassword();
        testProfileUpdateRejectsTakenEmail();
        testProfileUpdateBlankPasswordKeepsOld();

        // ── Module 8 : Admin — Inventory Management ───────────────────────────
        testSection("MODULE 8 — ADMIN INVENTORY MANAGEMENT");
        testRestockProduct();
        testRestockByNonAdmin();
        testReduceStockWithRemarks();
        testReduceStockNoRemarks();
        testReduceStockZeroAmount();
        testReduceStockAlreadyZero();
        testUndoRestock();
        testUndoReduceStock();
        testUndoWhenNothingToUndo();
        testAdminActivityHistory();
        testUpdateProductStockDirect();

        // ── Module 9 : Admin — User Management ────────────────────────────────
        testSection("MODULE 9 — ADMIN USER MANAGEMENT");
        testAdminCreateCustomer();
        testAdminCreateCustomerEmptyName();
        testAdminCreateCustomerDuplicateEmail();
        testAdminCreateCustomerShortPassword();
        testAdminUpdateCustomer();
        testAdminUpdateCustomerEmailConflict();
        testAdminUpdateCustomerShortPassword();
        testAdminDeleteCustomer();
        testAdminDeleteSelf();
        testAdminDeleteNonexistentUser();
        testAdminGetAllUsers();
        testNonAdminCannotManageUsers();
        testAdminViewCustomerWishlist();

        // ── Module 10 : Inventory Stock Status Labels ──────────────────────────
        testSection("MODULE 10 — INVENTORY STOCK STATUS");
        testStockLabelOutOfStock();
        testStockLabelLowStock();
        testStockLabelOk();
        testStockStatusBoundaries();
        testStockStatusHelpers();

        // ── Module 11 : Observer / Event System ───────────────────────────────
        testSection("MODULE 11 — OBSERVER / EVENT SYSTEM");
        testInventorySubjectOutOfStock();
        testInventorySubjectLowStock();
        testInventorySubjectOkNoNotification();
        testObserverDetach();
        testMultipleObservers();
        testAdminLowStockLoginSummary();
        testAdminLowStockLoginSummaryAsCustomer();

        // ── Module 12 : Design Patterns ───────────────────────────────────────
        testSection("MODULE 12 — DESIGN PATTERNS");
        testCartSingletonSameInstance();
        testCartSingletonDifferentUsers();
        testPaymentStrategiesAllSupported();

        // ─── Final Report ──────────────────────────────────────────────────────
        printReport();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 1 — AUTHENTICATION
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testLoginSuccess() {
        ShopEaseService s = service();
        check("Login with valid admin credentials succeeds",
              s.login("admin@email.admin.my", "adminpass"));
        check("getCurrentUser returns non-null after login",
              s.getCurrentUser() != null);
    }

    private static void testLoginWrongPassword() {
        ShopEaseService s = service();
        check("Login with wrong password fails",
              !s.login("admin@email.admin.my", "WRONG"));
        check("getCurrentUser is null after failed login",
              s.getCurrentUser() == null);
        check("getLastMessage reports failure",
              s.getLastMessage().toLowerCase().contains("invalid"));
    }

    private static void testLoginNonexistentEmail() {
        ShopEaseService s = service();
        check("Login with nonexistent email fails",
              !s.login("ghost@nowhere.com", "anypass"));
    }

    private static void testLoginNullInputs() {
        ShopEaseService s = service();
        check("Login with null email fails gracefully",
              !s.login(null, "pass"));
        check("Login with null password fails gracefully",
              !s.login("admin@email.admin.my", null));
    }

    private static void testLoginEmailCaseInsensitive() {
        ShopEaseService s = service();
        check("Login is case-insensitive for email",
              s.login("ADMIN@EMAIL.ADMIN.MY", "adminpass"));
    }

    private static void testLoginTrimsWhitespace() {
        ShopEaseService s = service();
        check("Login trims leading/trailing email whitespace",
              s.login("  admin@email.admin.my  ", "adminpass"));
    }

    private static void testLogout() {
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        s.logout();
        check("getCurrentUser is null after logout",
              s.getCurrentUser() == null);
        check("Cart is cleared after logout (getCartItemCount == 0)",
              s.getCartItemCount() == 0);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 2 — REGISTRATION
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testRegisterSuccess() {
        ShopEaseService s = service();
        long ts = ts();
        boolean ok = s.registerCustomer(new Customer("CUST-REG-" + ts, "Test User", "reg" + ts + "@test.my", "pass1234"));
        check("Registration with valid data succeeds", ok);
        check("Success message set", s.getLastMessage().toLowerCase().contains("created") || s.getLastMessage().toLowerCase().contains("success"));
    }

    private static void testRegisterDuplicateEmail() {
        ShopEaseService s = service();
        long ts = ts();
        String email = "dup" + ts + "@test.my";
        s.registerCustomer(new Customer("CUST-D1-" + ts, "User1", email, "pass1234"));
        boolean ok = s.registerCustomer(new Customer("CUST-D2-" + ts, "User2", email, "pass1234"));
        check("Duplicate email registration is rejected", !ok);
        check("Duplicate email error message set", s.getLastMessage().toLowerCase().contains("exist") || s.getLastMessage().toLowerCase().contains("already"));
    }

    private static void testRegisterNullCustomer() {
        ShopEaseService s = service();
        check("Null customer registration fails gracefully", !s.registerCustomer(null));
    }

    private static void testRegisterEmptyName() {
        ShopEaseService s = service();
        long ts = ts();
        check("Registration with empty name fails",
              !s.registerCustomer(new Customer("CUST-EN-" + ts, "", "en" + ts + "@test.my", "pass1234")));
        check("Empty name error message",
              s.getLastMessage().toLowerCase().contains("name"));
    }

    private static void testRegisterInvalidEmail() {
        ShopEaseService s = service();
        long ts = ts();
        check("Registration with invalid email fails",
              !s.registerCustomer(new Customer("CUST-IE-" + ts, "Bad Email", "notanemail", "pass1234")));
        check("Invalid email error message",
              s.getLastMessage().toLowerCase().contains("email"));
    }

    private static void testRegisterShortPassword() {
        ShopEaseService s = service();
        long ts = ts();
        check("Registration with password < 4 chars fails",
              !s.registerCustomer(new Customer("CUST-SP-" + ts, "Short Pass", "sp" + ts + "@test.my", "abc")));
        check("Short password error message",
              s.getLastMessage().toLowerCase().contains("password"));
    }

    private static void testRegisterEmailCaseInsensitive() {
        ShopEaseService s = service();
        long ts = ts();
        String email1 = "casechk" + ts + "@test.my";
        String email2 = "CASECHK" + ts + "@TEST.MY";
        s.registerCustomer(new Customer("CUST-CC1-" + ts, "Case One", email1, "pass1234"));
        check("Registration normalises email to lowercase — duplicate detected",
              !s.registerCustomer(new Customer("CUST-CC2-" + ts, "Case Two", email2, "pass1234")));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 3 — PRODUCT CATALOGUE
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testGetAllProducts() {
        ShopEaseService s = service();
        List<Product> products = s.getAllProducts();
        check("getAllProducts returns at least 3 seeded products", products.size() >= 3);
    }

    private static void testGetProductById() {
        ShopEaseService s = service();
        Product p = s.getProductById("MSE-02");
        check("getProductById returns correct product", p != null);
        check("Product name matches seed data", "Wireless Mouse".equals(p.getName()));
        check("Product price is positive", p.getPrice() > 0);
    }

    private static void testGetProductByInvalidId() {
        ShopEaseService s = service();
        Product p = s.getProductById("INVALID-999");
        check("getProductById with invalid ID returns null", p == null);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 4 — SHOPPING CART
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testAddToCartBasic() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        Product p = s.getProductById("MSE-02");
        check("addToCart succeeds for valid product", s.addToCart(p, 2));
        check("Cart item count is 2", s.getCartItemCount() == 2);
    }

    private static void testAddToCartAccumulates() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        Product p = s.getProductById("MSE-02");
        s.addToCart(p, 2);
        check("addToCart second time merges into existing cart item", s.addToCart(p, 3));
        check("Cart item count accumulated to 5", s.getCartItemCount() == 5);
    }

    private static void testAddToCartExceedsStock() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 5);
        Product p = s.getProductById("MSE-02");
        // set stock exactly to 5
        s.logout();
        setExactStock("MSE-02", 5);
        reLoginAsCustomer(s);

        check("addToCart rejects quantity exceeding stock", !s.addToCart(p, 6));
        check("Error message mentions available quantity", s.getLastMessage().toLowerCase().contains("available") || s.getLastMessage().toLowerCase().contains("only"));
        ensureStock("MSE-02", 20); // restore
    }

    private static void testAddToCartZeroQty() {
        ShopEaseService s = customerSession();
        Product p = s.getProductById("MSE-02");
        check("addToCart with qty=0 fails", !s.addToCart(p, 0));
    }

    private static void testAddToCartNegativeQty() {
        ShopEaseService s = customerSession();
        Product p = s.getProductById("MSE-02");
        check("addToCart with qty=-1 fails", !s.addToCart(p, -1));
    }

    private static void testAddToCartNullProduct() {
        ShopEaseService s = customerSession();
        check("addToCart with null product fails gracefully", !s.addToCart(null, 1));
    }

    private static void testAddToCartOutOfStock() {
        ShopEaseService s = customerSession();
        setExactStock("MSE-02", 0);
        Product p = s.getProductById("MSE-02");
        check("addToCart for out-of-stock product fails", !s.addToCart(p, 1));
        check("Error message mentions out of stock", s.getLastMessage().toLowerCase().contains("stock") || s.getLastMessage().toLowerCase().contains("out"));
        ensureStock("MSE-02", 20);
    }

    private static void testAdminCannotUseCart() {
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        Product p = s.getProductById("MSE-02");
        check("Admin cannot add to cart", !s.addToCart(p, 1));
        check("Admin cart message mentions customers only",
              s.getLastMessage().toLowerCase().contains("customer") || s.getLastMessage().toLowerCase().contains("cart"));
    }

    private static void testRemoveFromCart() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        Product p = s.getProductById("MSE-02");
        s.addToCart(p, 2);
        check("removeFromCart returns true", s.removeFromCart("MSE-02"));
        check("Cart is empty after remove", s.getCartItemCount() == 0);
    }

    private static void testRemoveNonexistentFromCart() {
        ShopEaseService s = customerSession();
        check("removeFromCart with non-existent productId returns false", !s.removeFromCart("NOT-IN-CART"));
    }

    private static void testUpdateCartQty() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 20);
        Product p = s.getProductById("MSE-02");
        s.addToCart(p, 2);
        check("updateCartQuantity changes qty", s.updateCartQuantity("MSE-02", 5));
        check("Cart count updated to 5", s.getCartItemCount() == 5);
    }

    private static void testUpdateCartQtyToZeroRemovesItem() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        Product p = s.getProductById("MSE-02");
        s.addToCart(p, 3);
        s.updateCartQuantity("MSE-02", 0);
        check("updateCartQuantity to 0 removes item", s.getCartItemCount() == 0);
    }

    private static void testUpdateCartQtyExceedsStock() {
        ShopEaseService s = customerSession();
        setExactStock("MSE-02", 3);
        Product p = s.getProductById("MSE-02");
        s.addToCart(p, 1);
        check("updateCartQuantity beyond stock is rejected", !s.updateCartQuantity("MSE-02", 10));
        ensureStock("MSE-02", 20);
    }

    private static void testClearCart() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        ensureStock("KBD-03", 10);
        s.addToCart(s.getProductById("MSE-02"), 2);
        s.addToCart(s.getProductById("KBD-03"), 1);
        s.clearCart();
        check("clearCart empties cart", s.getCartItemCount() == 0);
    }

    private static void testCartTotal() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        ensureStock("KBD-03", 10);
        Product mouse = s.getProductById("MSE-02");
        Product kbd   = s.getProductById("KBD-03");
        s.addToCart(mouse, 2); // 2 x 50 = 100
        s.addToCart(kbd, 1);   // 1 x 150 = 150
        double expected = mouse.getPrice() * 2 + kbd.getPrice() * 1;
        double actual   = s.getCartTotal();
        check("getCartTotal calculates correctly (expected " + expected + " got " + actual + ")",
              Math.abs(actual - expected) < 0.01);
    }

    private static void testGetCartItemCount() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        Product p = s.getProductById("MSE-02");
        s.addToCart(p, 3);
        check("getCartItemCount == 3", s.getCartItemCount() == 3);
        s.addToCart(p, 2);
        check("getCartItemCount == 5 after second add", s.getCartItemCount() == 5);
    }

    private static void testCartReminderMessageSingular() throws Exception {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        List<String> msgs = new ArrayList<>();
        s.attachObserver((evt, detail) -> { if ("CART_REMINDER".equals(evt)) msgs.add(detail); });
        s.addToCart(s.getProductById("MSE-02"), 1);
        s.publishCartReminderIfNeeded();
        Thread.sleep(50);
        check("Cart reminder fired with 1 item", msgs.size() >= 1);
        check("Reminder message says '1 item'", msgs.get(0).contains("1 item"));
    }

    private static void testCartReminderMessagePlural() throws Exception {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        List<String> msgs = new ArrayList<>();
        s.attachObserver((evt, detail) -> { if ("CART_REMINDER".equals(evt)) msgs.add(detail); });
        s.addToCart(s.getProductById("MSE-02"), 3);
        s.publishCartReminderIfNeeded();
        Thread.sleep(50);
        check("Cart reminder plural message",
              !msgs.isEmpty() && msgs.get(0).contains("3 item"));
    }

    private static void testCartReminderEmpty() throws Exception {
        ShopEaseService s = customerSession();
        List<String> msgs = new ArrayList<>();
        s.attachObserver((evt, detail) -> { if ("CART_REMINDER".equals(evt)) msgs.add(detail); });
        s.publishCartReminderIfNeeded();
        Thread.sleep(50);
        check("No cart reminder for empty cart", msgs.isEmpty());
    }

    private static void testSyncCartRemovesOutOfStockItem() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        s.addToCart(s.getProductById("MSE-02"), 2);
        setExactStock("MSE-02", 0); // go out of stock behind the scenes
        s.syncCartWithDatabase();
        check("syncCart removes out-of-stock item", s.getCartItemCount() == 0);
        ensureStock("MSE-02", 20);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 5 — CHECKOUT & ORDERS
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testCheckoutCreditCard() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        check("Checkout via CreditCard succeeds", s.checkout(new ShopEaseCreditCardStrategy()));
        check("Success message contains order ID", s.getLastMessage().toLowerCase().contains("order"));
    }

    private static void testCheckoutDuitNow() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        check("Checkout via DuitNow succeeds", s.checkout(new ShopEaseDuitNowStrategy()));
    }

    private static void testCheckoutMAE() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        check("Checkout via MAE succeeds", s.checkout(new ShopEaseMAEStrategy()));
    }

    private static void testCheckoutTNG() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        check("Checkout via Touch 'n Go succeeds", s.checkout(new ShopEaseTNGStrategy()));
    }

    private static void testCheckoutEmptyCart() {
        ShopEaseService s = customerSession();
        check("Checkout with empty cart fails", !s.checkout(new ShopEaseCreditCardStrategy()));
        check("Empty cart message set", s.getLastMessage().toLowerCase().contains("empty"));
    }

    private static void testCheckoutDeductsStock() {
        ensureStock("KBD-03", 20);
        ShopEaseService s = customerWithItemInCart("KBD-03", 2);
        int before = s.getProductById("KBD-03").getStockQuantity();
        s.checkout(new ShopEaseCreditCardStrategy());
        int after = s.getProductById("KBD-03").getStockQuantity();
        check("Checkout deducts stock (before=" + before + ", after=" + after + ")",
              after == before - 2);
    }

    private static void testCheckoutClearsCart() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        s.checkout(new ShopEaseCreditCardStrategy());
        check("Checkout clears cart", s.getCartItemCount() == 0);
    }

    private static void testOrderIdFormat() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        s.checkout(new ShopEaseCreditCardStrategy());
        List<Order> orders = s.getOrderHistory();
        check("Order history not empty after checkout", !orders.isEmpty());
        String orderId = orders.get(0).getOrderId();
        check("Order ID matches dd/MM/yy-NNN format: " + orderId,
              orderId.matches("\\d{2}/\\d{2}/\\d{2}-\\d{3}"));
    }

    private static void testOrderHistory() {
        ShopEaseService s = customerWithItemInCart("MSE-02", 1);
        s.checkout(new ShopEaseCreditCardStrategy());
        List<Order> orders = s.getOrderHistory();
        check("getOrderHistory returns at least 1 order", !orders.isEmpty());
        Order o = orders.get(0);
        check("Order total > 0", o.getTotalAmount() > 0);
        check("Order status is Completed", "Completed".equals(o.getStatus()));
        check("Order has items", o.getItems() != null && !o.getItems().isEmpty());
    }

    private static void testAdminViewCustomerOrders() {
        long ts = ts();
        String custEmail = "ordcust" + ts + "@test.my";
        String custId    = "CUST-ORD-" + ts;
        ShopEaseService cust = service();
        cust.registerCustomer(new Customer(custId, "Order Cust", custEmail, "pass1234"));
        cust.login(custEmail, "pass1234");
        ensureStock("MSE-02", 5);
        cust.addToCart(cust.getProductById("MSE-02"), 1);
        cust.checkout(new ShopEaseCreditCardStrategy());
        cust.logout();

        ShopEaseService admin = service();
        admin.login("admin@email.admin.my", "adminpass");
        List<Order> orders = admin.getOrdersForUserAsAdmin(custId);
        check("Admin can view customer's orders", !orders.isEmpty());
    }

    private static void testAdminViewCustomerOrdersNotAdmin() {
        ShopEaseService s = customerSession();
        List<Order> orders = s.getOrdersForUserAsAdmin("ADMIN-001");
        check("Non-admin cannot view orders via admin API", orders.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 6 — WISHLIST
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testWishlistAdd() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        check("addToWishlist succeeds", s.addToWishlist("MSE-02"));
        check("getLastMessage confirms add", s.getLastMessage().toLowerCase().contains("added") || s.getLastMessage().toLowerCase().contains("wishlist"));
    }

    private static void testWishlistDuplicate() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        s.addToWishlist("MSE-02");
        check("Duplicate addToWishlist is rejected", !s.addToWishlist("MSE-02"));
    }

    private static void testWishlistRemove() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        s.addToWishlist("MSE-02");
        check("removeFromWishlist returns true", s.removeFromWishlist("MSE-02"));
        check("Product no longer in wishlist", !s.isInWishlist("MSE-02"));
    }

    private static void testWishlistIsInWishlist() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        check("isInWishlist false before add", !s.isInWishlist("MSE-02"));
        s.addToWishlist("MSE-02");
        check("isInWishlist true after add", s.isInWishlist("MSE-02"));
    }

    private static void testWishlistFakeProduct() {
        ShopEaseService s = customerSession();
        check("addToWishlist with fake productId fails", !s.addToWishlist("FAKE-PRODUCT"));
        check("Fake product error message set",
              s.getLastMessage().toLowerCase().contains("not found") || s.getLastMessage().toLowerCase().contains("product"));
    }

    private static void testWishlistAdminBlocked() {
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        check("Admin cannot add to wishlist", !s.addToWishlist("MSE-02"));
        check("Wishlist blocked message set",
              s.getLastMessage().toLowerCase().contains("customer"));
    }

    private static void testMoveWishlistItemToCart() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        s.addToWishlist("MSE-02");
        check("moveWishlistItemToCart succeeds", s.moveWishlistItemToCart("MSE-02", 1));
        check("Item removed from wishlist after move", !s.isInWishlist("MSE-02"));
        check("Item appears in cart", s.getCartItemCount() == 1);
    }

    private static void testMoveWishlistFakeProduct() {
        ShopEaseService s = customerSession();
        check("moveWishlistItemToCart with fake productId fails", !s.moveWishlistItemToCart("FAKE-PRODUCT", 1));
    }

    private static void testGetWishlistProducts() {
        ShopEaseService s = customerSession();
        ensureStock("MSE-02", 10);
        ensureStock("KBD-03", 10);
        s.addToWishlist("MSE-02");
        s.addToWishlist("KBD-03");
        List<Product> wl = s.getWishlistProducts();
        check("getWishlistProducts returns 2 items", wl.size() == 2);
    }

    private static void testWishlistRestockNotification() throws Exception {
        long ts = ts();
        String email  = "restock" + ts + "@test.my";
        String userId = "CUST-RS-" + ts;

        // Admin sets mouse stock to 0
        setExactStock("MSE-02", 0);

        // Customer adds out-of-stock mouse to wishlist
        ShopEaseService cust = service();
        cust.registerCustomer(new Customer(userId, "Restock Cust", email, "pass1234"));
        cust.login(email, "pass1234");
        cust.addToWishlist("MSE-02");
        cust.logout();

        // Admin restocks
        ensureStock("MSE-02", 10);

        // Customer logs back in — should get wishlist restock event
        List<String> msgs = new ArrayList<>();
        cust = service();
        cust.login(email, "pass1234");
        cust.attachObserver((evt, detail) -> { if ("WISHLIST_RESTOCK".equals(evt)) msgs.add(detail); });
        cust.publishWishlistRestockOnLogin();
        Thread.sleep(100);
        check("Wishlist restock notification fired on login", msgs.size() == 1);
        check("Restock message contains product name", msgs.get(0).contains("Wireless Mouse"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 7 — CUSTOMER PROFILE
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testProfileUpdateAllFields() {
        long ts = ts();
        String origEmail = "prof" + ts + "@test.my";
        String newEmail  = "profnew" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PR-" + ts, "Old Name", origEmail, "pass1234"));
        s.login(origEmail, "pass1234");
        check("Profile update (name+email+password) succeeds",
              s.updateCustomerProfile("New Name", newEmail, "newpass99"));
        check("Name persisted", "New Name".equals(s.getCurrentUser().getName()));
        s.logout();
        check("New credentials work after update", s.login(newEmail, "newpass99"));
    }

    private static void testProfileUpdateNameOnly() {
        long ts = ts();
        String email = "profname" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PN-" + ts, "Original", email, "pass1234"));
        s.login(email, "pass1234");
        check("Profile update name only (blank password) succeeds",
              s.updateCustomerProfile("Updated Name", email, ""));
        check("Name changed", "Updated Name".equals(s.getCurrentUser().getName()));
        s.logout();
        check("Old password still works", s.login(email, "pass1234"));
    }

    private static void testProfileUpdateRejectsEmptyName() {
        long ts = ts();
        String email = "profep" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PE-" + ts, "Legit", email, "pass1234"));
        s.login(email, "pass1234");
        check("Profile update rejects empty name",
              !s.updateCustomerProfile("", email, "pass1234"));
        check("Error mentions name", s.getLastMessage().toLowerCase().contains("name"));
    }

    private static void testProfileUpdateRejectsBadEmail() {
        long ts = ts();
        String email = "profe" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PB-" + ts, "User", email, "pass1234"));
        s.login(email, "pass1234");
        check("Profile update rejects invalid email",
              !s.updateCustomerProfile("User", "notanemail", "pass1234"));
        check("Error mentions email", s.getLastMessage().toLowerCase().contains("email"));
    }

    private static void testProfileUpdateRejectsShortPassword() {
        long ts = ts();
        String email = "profsp" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PS-" + ts, "User", email, "pass1234"));
        s.login(email, "pass1234");
        check("Profile update rejects password shorter than 4 chars",
              !s.updateCustomerProfile("User", email, "ab"));
        check("Error mentions password", s.getLastMessage().toLowerCase().contains("password"));
    }

    private static void testProfileUpdateRejectsTakenEmail() {
        long ts = ts();
        String email1 = "proft1" + ts + "@test.my";
        String email2 = "proft2" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PT1-" + ts, "User1", email1, "pass1234"));
        s.registerCustomer(new Customer("CUST-PT2-" + ts, "User2", email2, "pass1234"));
        s.login(email1, "pass1234");
        check("Profile update rejects email taken by another user",
              !s.updateCustomerProfile("User1", email2, "pass1234"));
    }

    private static void testProfileUpdateBlankPasswordKeepsOld() {
        long ts = ts();
        String email = "profblank" + ts + "@test.my";
        ShopEaseService s = service();
        s.registerCustomer(new Customer("CUST-PBL-" + ts, "User", email, "original99"));
        s.login(email, "original99");
        s.updateCustomerProfile("User", email, ""); // blank password
        s.logout();
        check("Old password still works when blank password submitted",
              s.login(email, "original99"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 8 — ADMIN INVENTORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testRestockProduct() {
        ShopEaseService s = adminSession();
        ensureStock("KBD-03", 10);
        int before = s.getProductById("KBD-03").getStockQuantity();
        check("restockProduct returns true", s.restockProduct("KBD-03", 5));
        int after = s.getProductById("KBD-03").getStockQuantity();
        check("Stock increased by 5", after == before + 5);
        check("Success message set", s.getLastMessage().toLowerCase().contains("restock") || s.getLastMessage().toLowerCase().contains("+5"));
    }

    private static void testRestockByNonAdmin() {
        ShopEaseService s = customerSession();
        check("Customer cannot restock", !s.restockProduct("MSE-02", 5));
        check("Error message mentions admin", s.getLastMessage().toLowerCase().contains("admin"));
    }

    private static void testReduceStockWithRemarks() {
        ShopEaseService s = adminSession();
        ensureStock("KBD-03", 20);
        int before = s.getProductById("KBD-03").getStockQuantity();
        check("reduceStockProduct with remarks succeeds",
              s.reduceStockProduct("KBD-03", 3, "audit test"));
        int after = s.getProductById("KBD-03").getStockQuantity();
        check("Stock reduced by 3", after == before - 3);
    }

    private static void testReduceStockNoRemarks() {
        ShopEaseService s = adminSession();
        ensureStock("KBD-03", 20);
        check("reduceStockProduct without remarks is rejected",
              !s.reduceStockProduct("KBD-03", 3, ""));
        check("Error message mentions remarks", s.getLastMessage().toLowerCase().contains("remark"));
    }

    private static void testReduceStockZeroAmount() {
        ShopEaseService s = adminSession();
        check("reduceStockProduct with 0 amount is rejected",
              !s.reduceStockProduct("KBD-03", 0, "zero test"));
        check("Error message mentions amount", s.getLastMessage().toLowerCase().contains("amount") || s.getLastMessage().toLowerCase().contains("greater"));
    }

    private static void testReduceStockAlreadyZero() {
        ShopEaseService s = adminSession();
        setExactStock("KBD-03", 0);
        check("reduceStockProduct on zero stock is rejected",
              !s.reduceStockProduct("KBD-03", 1, "already zero"));
        ensureStock("KBD-03", 20);
    }

    private static void testUndoRestock() {
        ShopEaseService s = adminSession();
        ensureStock("KBD-03", 10);
        int before = s.getProductById("KBD-03").getStockQuantity();
        s.restockProduct("KBD-03", 7);
        check("canUndoLastRestock is true", s.canUndoLastRestock());
        check("undoLastRestock succeeds", s.undoLastRestock());
        int after = s.getProductById("KBD-03").getStockQuantity();
        check("Stock restored to original value", after == before);
        check("canUndoLastRestock is false after undo", !s.canUndoLastRestock());
    }

    private static void testUndoReduceStock() {
        ShopEaseService s = adminSession();
        ensureStock("KBD-03", 15);
        int before = s.getProductById("KBD-03").getStockQuantity();
        s.reduceStockProduct("KBD-03", 5, "demo reduce");
        check("undoLastRestock also undoes reduce", s.undoLastRestock());
        int after = s.getProductById("KBD-03").getStockQuantity();
        check("Stock restored after undo reduce (before=" + before + " after=" + after + ")", after == before);
    }

    private static void testUndoWhenNothingToUndo() {
        ShopEaseService s = adminSession();
        check("undoLastRestock with nothing to undo returns false", !s.undoLastRestock());
        check("Error message says nothing to undo", s.getLastMessage().toLowerCase().contains("nothing") || s.getLastMessage().toLowerCase().contains("undo"));
    }

    private static void testAdminActivityHistory() {
        ShopEaseService s = adminSession();
        s.restockProduct("MSE-02", 1);
        List<AdminInventoryLog> logs = s.getAdminActivityHistory();
        check("Admin activity history is non-empty", !logs.isEmpty());
        check("Log entry has productId", logs.get(0).getProductId() != null);
    }

    private static void testUpdateProductStockDirect() {
        ShopEaseService s = adminSession();
        s.updateProductStock("MSE-02", "Wireless Mouse", 15);
        Product p = s.getProductById("MSE-02");
        check("updateProductStock sets exact stock", p.getStockQuantity() == 15);
        ensureStock("MSE-02", 20);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 9 — ADMIN USER MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testAdminCreateCustomer() {
        ShopEaseService s = adminSession();
        long ts = ts();
        String email = "admcreate" + ts + "@test.my";
        check("Admin creates customer successfully",
              s.createCustomerAsAdmin("Admin Created", email, "pass1234"));
        check("Success message set", s.getLastMessage().toLowerCase().contains("created") || s.getLastMessage().toLowerCase().contains("customer"));
    }

    private static void testAdminCreateCustomerEmptyName() {
        ShopEaseService s = adminSession();
        long ts = ts();
        check("Admin create customer with empty name is rejected",
              !s.createCustomerAsAdmin("", "e" + ts + "@test.my", "pass1234"));
        check("Error mentions name", s.getLastMessage().toLowerCase().contains("name"));
    }

    private static void testAdminCreateCustomerDuplicateEmail() {
        ShopEaseService s = adminSession();
        long ts = ts();
        String email = "admdup" + ts + "@test.my";
        s.createCustomerAsAdmin("First", email, "pass1234");
        check("Admin create customer with duplicate email fails",
              !s.createCustomerAsAdmin("Second", email, "pass1234"));
    }

    private static void testAdminCreateCustomerShortPassword() {
        ShopEaseService s = adminSession();
        long ts = ts();
        check("Admin create customer with short password fails",
              !s.createCustomerAsAdmin("Name", "sp" + ts + "@test.my", "ab"));
        check("Error mentions password", s.getLastMessage().toLowerCase().contains("password"));
    }

    private static void testAdminUpdateCustomer() {
        long ts = ts();
        String origEmail = "admupd" + ts + "@test.my";
        String newEmail  = "admupdnew" + ts + "@test.my";
        ShopEaseService s = adminSession();
        s.createCustomerAsAdmin("Original Name", origEmail, "pass1234");
        // find user id
        String userId = s.getAllUsersForAdmin().stream()
                .filter(u -> origEmail.equals(u.getEmail()))
                .map(User::getUserId)
                .findFirst().orElse(null);
        check("User found for admin update", userId != null);
        check("Admin updates customer",
              s.updateCustomerAsAdmin(userId, "New Name", newEmail, "newpass99"));
        check("Success message set", s.getLastMessage().toLowerCase().contains("updated") || s.getLastMessage().toLowerCase().contains("customer"));
    }

    private static void testAdminUpdateCustomerEmailConflict() {
        long ts = ts();
        String emailA = "adma" + ts + "@test.my";
        String emailB = "admb" + ts + "@test.my";
        ShopEaseService s = adminSession();
        s.createCustomerAsAdmin("UserA", emailA, "pass1234");
        s.createCustomerAsAdmin("UserB", emailB, "pass1234");
        String userBId = s.getAllUsersForAdmin().stream()
                .filter(u -> emailB.equals(u.getEmail()))
                .map(User::getUserId)
                .findFirst().orElse(null);
        check("Admin update rejected when email taken by another user",
              !s.updateCustomerAsAdmin(userBId, "UserB", emailA, "pass1234"));
    }

    private static void testAdminUpdateCustomerShortPassword() {
        long ts = ts();
        String email = "admupdsp" + ts + "@test.my";
        ShopEaseService s = adminSession();
        s.createCustomerAsAdmin("UserSP", email, "pass1234");
        String userId = s.getAllUsersForAdmin().stream()
                .filter(u -> email.equals(u.getEmail()))
                .map(User::getUserId)
                .findFirst().orElse(null);
        check("Admin update rejected with short password",
              !s.updateCustomerAsAdmin(userId, "UserSP", email, "ab"));
        check("Error mentions password", s.getLastMessage().toLowerCase().contains("password"));
    }

    private static void testAdminDeleteCustomer() {
        long ts = ts();
        String email  = "admdelete" + ts + "@test.my";
        String custId = "CUST-ADMDELETE-" + ts;
        // register via customer path
        ShopEaseService reg = service();
        reg.registerCustomer(new Customer(custId, "To Delete", email, "pass1234"));

        ShopEaseService s = adminSession();
        check("Admin deletes customer successfully", s.deleteUserAsAdmin(custId));
        // cannot log in after deletion
        ShopEaseService login = service();
        check("Deleted user cannot log in", !login.login(email, "pass1234"));
    }

    private static void testAdminDeleteSelf() {
        ShopEaseService s = adminSession();
        check("Admin cannot delete own account", !s.deleteUserAsAdmin("ADMIN-001"));
        check("Error message mentions self-delete",
              s.getLastMessage().toLowerCase().contains("delete") || s.getLastMessage().toLowerCase().contains("own") || s.getLastMessage().toLowerCase().contains("cannot"));
    }

    private static void testAdminDeleteNonexistentUser() {
        ShopEaseService s = adminSession();
        check("Admin delete with nonexistent ID returns false",
              !s.deleteUserAsAdmin("DOES-NOT-EXIST"));
        check("Error mentions user not found", s.getLastMessage().toLowerCase().contains("not found") || s.getLastMessage().toLowerCase().contains("user"));
    }

    private static void testAdminGetAllUsers() {
        ShopEaseService s = adminSession();
        List<User> users = s.getAllUsersForAdmin();
        // Should contain only customers
        check("getAllUsersForAdmin returns customers (no admins)",
              users.stream().noneMatch(u -> u instanceof Admin));
    }

    private static void testNonAdminCannotManageUsers() {
        ShopEaseService s = customerSession();
        check("Non-admin getAllUsersForAdmin returns empty list", s.getAllUsersForAdmin().isEmpty());
        check("Non-admin createCustomerAsAdmin returns false",
              !s.createCustomerAsAdmin("X", "x@x.com", "pass1234"));
        check("Non-admin deleteUserAsAdmin returns false",
              !s.deleteUserAsAdmin("ADMIN-001"));
    }

    private static void testAdminViewCustomerWishlist() {
        long ts = ts();
        String email  = "wishview" + ts + "@test.my";
        String custId = "CUST-WV-" + ts;
        ShopEaseService cust = service();
        cust.registerCustomer(new Customer(custId, "Wish View", email, "pass1234"));
        cust.login(email, "pass1234");
        ensureStock("MSE-02", 10);
        cust.addToWishlist("MSE-02");
        cust.logout();

        ShopEaseService admin = adminSession();
        List<Product> wl = admin.getWishlistForUserAsAdmin(custId);
        check("Admin can view customer wishlist", !wl.isEmpty());
        check("Admin wishlist contains Wireless Mouse",
              wl.stream().anyMatch(p -> "MSE-02".equals(p.getProductId())));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 10 — INVENTORY STOCK STATUS
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testStockLabelOutOfStock() {
        check("label(0) == 'NO STOCK'", "NO STOCK".equals(InventoryStockStatus.label(0)));
        check("label(-5) == 'NO STOCK'", "NO STOCK".equals(InventoryStockStatus.label(-5)));
    }

    private static void testStockLabelLowStock() {
        check("label(1) == 'LOW STOCK'", "LOW STOCK".equals(InventoryStockStatus.label(1)));
        check("label(4) == 'LOW STOCK'", "LOW STOCK".equals(InventoryStockStatus.label(4)));
    }

    private static void testStockLabelOk() {
        check("label(5) == 'OK'", "OK".equals(InventoryStockStatus.label(5)));
        check("label(100) == 'OK'", "OK".equals(InventoryStockStatus.label(100)));
    }

    private static void testStockStatusBoundaries() {
        // Threshold is 5
        check("isLowStock(4) is true",  InventoryStockStatus.isLowStock(4));
        check("isLowStock(5) is false", !InventoryStockStatus.isLowStock(5));
        check("isOk(5) is true",        InventoryStockStatus.isOk(5));
        check("isOk(4) is false",       !InventoryStockStatus.isOk(4));
        check("isOutOfStock(0) is true",  InventoryStockStatus.isOutOfStock(0));
        check("isOutOfStock(1) is false", !InventoryStockStatus.isOutOfStock(1));
    }

    private static void testStockStatusHelpers() {
        check("isOutOfStock(-1) is true", InventoryStockStatus.isOutOfStock(-1));
        check("isLowStock(0) is false",   !InventoryStockStatus.isLowStock(0));
        check("isOk(0) is false",         !InventoryStockStatus.isOk(0));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 11 — OBSERVER / EVENT SYSTEM
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testInventorySubjectOutOfStock() {
        ShopEaseInventorySubject subject = new ShopEaseInventorySubject();
        List<String> events = new ArrayList<>();
        subject.attach((evt, name) -> events.add(evt));
        subject.setStock(0, "Test Product");
        check("OUT_OF_STOCK event fired when stock=0", events.contains("OUT_OF_STOCK"));
    }

    private static void testInventorySubjectLowStock() {
        ShopEaseInventorySubject subject = new ShopEaseInventorySubject();
        List<String> events = new ArrayList<>();
        subject.attach((evt, name) -> events.add(evt));
        subject.setStock(3, "Test Product");
        check("LOW_STOCK event fired when stock=3", events.contains("LOW_STOCK"));
    }

    private static void testInventorySubjectOkNoNotification() {
        ShopEaseInventorySubject subject = new ShopEaseInventorySubject();
        List<String> events = new ArrayList<>();
        subject.attach((evt, name) -> events.add(evt));
        subject.setStock(10, "Test Product");
        check("No event fired when stock is OK (=10)", events.isEmpty());
    }

    private static void testObserverDetach() {
        ShopEaseInventorySubject subject = new ShopEaseInventorySubject();
        AtomicInteger count = new AtomicInteger(0);
        var observer = (com.shopease.observer.ShopEaseInventoryObserver) (evt, name) -> count.incrementAndGet();
        subject.attach(observer);
        subject.setStock(0, "Item");
        check("Observer notified before detach", count.get() == 1);
        subject.detach(observer);
        subject.setStock(0, "Item2");
        check("Observer NOT notified after detach", count.get() == 1);
    }

    private static void testMultipleObservers() {
        ShopEaseInventorySubject subject = new ShopEaseInventorySubject();
        AtomicInteger a = new AtomicInteger(0);
        AtomicInteger b = new AtomicInteger(0);
        subject.attach((evt, name) -> a.incrementAndGet());
        subject.attach((evt, name) -> b.incrementAndGet());
        subject.setStock(0, "Product");
        check("First observer notified",  a.get() == 1);
        check("Second observer notified", b.get() == 1);
    }

    private static void testAdminLowStockLoginSummary() throws Exception {
        setExactStock("MSE-02", 2); // force low stock
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        List<String> events = new ArrayList<>();
        s.attachObserver((evt, detail) -> events.add(evt));
        s.publishAdminLowStockOnLogin();
        Thread.sleep(50);
        check("ADMIN_LOGIN_STOCK event fired when low/out stock exists",
              events.contains("ADMIN_LOGIN_STOCK"));
        ensureStock("MSE-02", 20);
    }

    private static void testAdminLowStockLoginSummaryAsCustomer() throws Exception {
        ShopEaseService s = customerSession();
        List<String> events = new ArrayList<>();
        s.attachObserver((evt, detail) -> events.add(evt));
        s.publishAdminLowStockOnLogin(); // should be no-op for customer
        Thread.sleep(50);
        check("publishAdminLowStockOnLogin is no-op for customer",
              events.isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   MODULE 12 — DESIGN PATTERNS
    // ═══════════════════════════════════════════════════════════════════════════

    private static void testCartSingletonSameInstance() {
        ShopEaseCartSingleton a = ShopEaseCartSingleton.getInstance("SINGLETON-USER-A");
        ShopEaseCartSingleton b = ShopEaseCartSingleton.getInstance("SINGLETON-USER-A");
        check("CartSingleton returns the same instance for the same user", a == b);
    }

    private static void testCartSingletonDifferentUsers() {
        ShopEaseCartSingleton a = ShopEaseCartSingleton.getInstance("SINGLETON-USER-X");
        ShopEaseCartSingleton b = ShopEaseCartSingleton.getInstance("SINGLETON-USER-Y");
        check("CartSingleton returns different instances for different users", a != b);
    }

    private static void testPaymentStrategiesAllSupported() {
        // Each strategy should execute without throwing
        ShopEasePaymentStrategy[] strategies = {
            new ShopEaseCreditCardStrategy(),
            new ShopEaseDuitNowStrategy(),
            new ShopEaseMAEStrategy(),
            new ShopEaseTNGStrategy()
        };
        String[] names = { "CreditCard", "DuitNow", "MAE", "TNG" };
        for (int i = 0; i < strategies.length; i++) {
            try {
                new ShopEasePaymentContext(strategies[i]).executeStrategy(100.00);
                check("Payment strategy executes without error: " + names[i], true);
            } catch (Exception e) {
                check("Payment strategy executes without error: " + names[i], false);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //   HELPERS
    // ═══════════════════════════════════════════════════════════════════════════

    private static ShopEaseService service() {
        return new ShopEaseService();
    }

    private static ShopEaseService adminSession() {
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        return s;
    }

    private static ShopEaseService customerSession() {
        ShopEaseService s = service();
        long ts = ts();
        String email = "tmp" + ts + "@test.my";
        s.registerCustomer(new Customer("CUST-TMP-" + ts, "Temp User", email, "pass1234"));
        s.login(email, "pass1234");
        return s;
    }

    /** Logs out and re-logs in as a freshly created customer on the same service instance. */
    private static void reLoginAsCustomer(ShopEaseService s) {
        // Already logged in as customer — just re-sync
        s.syncCartWithDatabase();
    }

    private static ShopEaseService customerWithItemInCart(String productId, int qty) {
        ensureStock(productId, qty + 5);
        ShopEaseService s = customerSession();
        Product p = s.getProductById(productId);
        s.addToCart(p, qty);
        return s;
    }

    private static void ensureStock(String productId, int minimum) {
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        Product p = s.getProductById(productId);
        if (p != null && p.getStockQuantity() < minimum) {
            s.restockProduct(productId, minimum - p.getStockQuantity());
        }
        s.logout();
    }

    private static void setExactStock(String productId, int qty) {
        ShopEaseService s = service();
        s.login("admin@email.admin.my", "adminpass");
        Product p = s.getProductById(productId);
        if (p == null) return;
        int current = p.getStockQuantity();
        if (current > qty) {
            s.reduceStockProduct(productId, current - qty, "test setup");
        } else if (current < qty) {
            s.restockProduct(productId, qty - current);
        }
        s.logout();
    }

    private static long ts() {
        return System.currentTimeMillis();
    }

    // ─── Assertion helpers ───────────────────────────────────────────────────

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            failures.add(description);
            System.err.println("  ✗ FAIL: " + description);
        }
    }

    // ─── Output helpers ──────────────────────────────────────────────────────

    private static void testSection(String title) {
        System.out.println("\n─── " + title + " ───");
    }

    private static void printBanner(String title) {
        System.out.println("═".repeat(60));
        System.out.println("  " + title);
        System.out.println("═".repeat(60));
    }

    private static void printReport() {
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  TEST RESULTS");
        System.out.println("═".repeat(60));
        System.out.println("  PASSED : " + passed);
        System.out.println("  FAILED : " + failed);
        System.out.println("  TOTAL  : " + (passed + failed));
        if (!failures.isEmpty()) {
            System.out.println("\n  FAILED TESTS:");
            for (String f : failures) {
                System.out.println("    ✗ " + f);
            }
        }
        System.out.println("═".repeat(60));
        if (failed > 0) {
            System.exit(1);
        } else {
            System.out.println("  ALL TESTS PASSED");
        }
    }
}
