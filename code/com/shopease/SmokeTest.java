package com.shopease;

import com.shopease.model.Customer;
import com.shopease.model.Product;
import com.shopease.service.ShopEaseService;
import com.shopease.strategy.ShopEaseCreditCardStrategy;

/** Quick non-GUI verification. Run: java -cp bin:lib/sqlite-jdbc.jar com.shopease.SmokeTest */
public class SmokeTest {
    public static void main(String[] args) {
        ShopEaseService s = new ShopEaseService();
        if (!s.login("admin@email.admin.my", "adminpass")) fail("admin login");
        if (s.getCart() != null) fail("admin should have no cart");

        long ts = System.currentTimeMillis();
        String email = "test" + ts + "@test.my";
        if (!s.registerCustomer(new Customer("CUST-" + ts, "Test", email, "pass"))) {
            fail("register: " + s.getLastMessage());
        }
        if (s.registerCustomer(new Customer("CUST-dup-" + ts, "Test2", email, "pass2"))) {
            fail("duplicate email should be rejected");
        }

        if (!s.login(email, "pass")) fail("customer login");
        Product mouse = s.getProductById("MSE-02");
        if (!s.addToCart(mouse, 2)) fail("add to cart");
        if (!s.checkout(new ShopEaseCreditCardStrategy())) fail("checkout: " + s.getLastMessage());

        int stock = s.getProductById("MSE-02").getStockQuantity();
        if (stock != mouse.getStockQuantity() - 2) fail("stock mismatch: " + stock);

        System.out.println("ALL SMOKE TESTS PASSED");
    }

    private static void fail(String msg) {
        System.err.println("FAIL: " + msg);
        System.exit(1);
    }
}
