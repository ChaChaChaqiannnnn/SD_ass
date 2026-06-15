package com.shopease.service;

import com.shopease.dao.AdminActivityDAO;
import com.shopease.dao.DatabaseConnection;
import com.shopease.dao.OrderDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dao.UserDAO;
import com.shopease.dao.WishlistDAO;
import com.shopease.model.*;
import com.shopease.observer.*;
import com.shopease.singleton.ShopEaseCartSingleton;
import com.shopease.singleton.ShopEaseWishlistSingleton;
import com.shopease.strategy.*;
import com.shopease.util.OrderIdGenerator;
import com.shopease.util.UserAccountUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The "brain" of ShopEase — sits between the UI screens and the database (DAO layer).
 * <p>
 * When you explain this class to your lecturer, say:
 * <ul>
 *   <li><b>Singleton</b> — on login we load one cart + one wishlist per customer; DAOs share {@code ShopEaseDatabaseManager}</li>
 *   <li><b>Observer</b> — stock changes and login events are published to popup/refresh observers</li>
 *   <li><b>Strategy</b> — payment, profile, admin user actions, login validation, and inventory actions swap algorithms at runtime</li>
 * </ul>
 */
public class ShopEaseService {
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private AdminActivityDAO adminActivityDAO;
    private WishlistDAO wishlistDAO;

    private User currentUser;
    private ShopEaseCartSingleton userCart;
    private ShopEaseWishlistSingleton userWishlist;
    private ShopEaseInventorySubject inventorySystem;
    // Strategy objects — we pick the algorithm once here, then call it when needed (no if-else chains in UI)
    private final ShopEaseProfileUpdateStrategy profileUpdateStrategy = new CustomerProfileUpdateStrategy();
    private final ShopEaseAdminUserActionStrategy deleteUserStrategy = new DeleteCustomerUserStrategy();
    private final ShopEaseCreateCustomerStrategy createUserStrategy = new CreateCustomerUserStrategy();
    private final ShopEaseUpdateCustomerStrategy updateUserStrategy = new UpdateCustomerUserStrategy();
    private final ShopEaseRegisterCustomerStrategy registerCustomerStrategy = new RegisterCustomerStrategy();
    private final ShopEaseLoginValidationStrategy loginValidationStrategy = new DefaultLoginValidationStrategy();
    private final ShopEaseInventoryActionStrategy restockStrategy = new RestockInventoryStrategy();
    private final ShopEaseInventoryActionStrategy reduceStrategy = new ReduceInventoryStrategy();
    private final ShopEaseInventoryActionStrategy undoStrategy = new UndoInventoryStrategy();
    private String lastMessage = "";
    private String lastOrderId = "";
    private AdminInventoryLog lastUndoableStockAction;

    public ShopEaseService() {
        DatabaseConnection.initializeDatabase();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
        this.orderDAO = new OrderDAO();
        this.adminActivityDAO = new AdminActivityDAO();
        this.wishlistDAO = new WishlistDAO();
        this.inventorySystem = new ShopEaseInventorySubject();
        seedInitialData();
    }

    private void seedInitialData() {
        if (userDAO.getUserByEmail("admin@email.admin.my") == null) {
            userDAO.insertUser(new Admin("ADMIN-001", "Admin", "admin@email.admin.my", "adminpass"));
        }
        if (productDAO.getAllProducts().isEmpty()) {
            productDAO.insertProduct(new Product("LPT-01", "Gaming Laptop", 2500.00, 5, "Electronics"));
            productDAO.insertProduct(new Product("MSE-02", "Wireless Mouse", 50.00, 20, "Accessories"));
            productDAO.insertProduct(new Product("KBD-03", "Mechanical Keyboard", 150.00, 10, "Accessories"));
        }
    }

    // ==================== LOGIN (Singleton + Observer setup happens here) ====================

    public boolean login(String email, String password) {
        lastMessage = "";
        ShopEaseLoginValidationStrategy.LoginValidationResult validationResult =
                new ShopEaseLoginValidationStrategy.LoginValidationResult();
        if (!loginValidationStrategy.validate(userDAO, email, password, validationResult)) {
            lastMessage = validationResult.message;
            return false;
        }
        User user = validationResult.user;
        this.currentUser = user;
        this.inventorySystem = new ShopEaseInventorySubject();
        this.userCart = null;
        this.lastUndoableStockAction = null;

        if (user instanceof Customer) {
            // Singleton — each customer gets exactly ONE in-memory cart and wishlist for this session
            userCart = ShopEaseCartSingleton.getInstance(user.getUserId());
            userWishlist = ShopEaseWishlistSingleton.getInstance(user.getUserId());
            // Observer — if stock hits zero, auto-remove that product from this customer's cart
            inventorySystem.attach(new ShopEaseCartStockSyncObserver(userCart));
        } else {
            userWishlist = null; // admins don't shop, so no cart/wishlist singleton
        }
        setupInventoryObservers(); // always attach the console admin log observer
        return true;
    }

    public boolean registerCustomer(User customer) {
        lastMessage = "";
        ShopEaseRegisterCustomerStrategy.RegisterCustomerResult result =
                new ShopEaseRegisterCustomerStrategy.RegisterCustomerResult();
        if (!registerCustomerStrategy.register(userDAO, customer, result)) {
            lastMessage = result.message;
            return false;
        }
        lastMessage = result.message;
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    /** Order ID from the most recent successful checkout, or empty if none. */
    public String getLastOrderId() {
        return lastOrderId == null ? "" : lastOrderId;
    }

    public void logout() {
        this.currentUser = null;
        this.userCart = null;
        this.userWishlist = null;
        this.inventorySystem = new ShopEaseInventorySubject();
        this.lastMessage = "";
        this.lastUndoableStockAction = null;
    }

    // ==================== OBSERVER PATTERN (subject notifies listeners) ====================

    private void setupInventoryObservers() {
        // This observer prints LOW/OUT stock messages to the console for admins
        inventorySystem.attach(new ShopEaseInventoryAdminLogObserver());
    }

    /** UI screens call this to register extra observers (popups, live refresh, login reminders). */
    public void attachObserver(Observer observer) {
        if (observer != null) {
            inventorySystem.attach(observer);
        }
    }

    public void detachObserver(Observer observer) {
        if (observer != null) {
            inventorySystem.detach(observer);
        }
    }

    /** Builds a summary list, then fires ADMIN_LOGIN_STOCK so the login popup observer can show it. */
    public void publishAdminLowStockOnLogin() {
        if (!(currentUser instanceof Admin)) {
            return;
        }
        StringBuilder summary = new StringBuilder();
        for (Product p : productDAO.getAllProducts()) {
            int stock = p.getStockQuantity();
            if (InventoryStockStatus.isOutOfStock(stock)) {
                if (summary.length() > 0) {
                    summary.append("\n");
                }
                summary.append("• ").append(p.getName()).append(" — OUT OF STOCK");
            } else if (InventoryStockStatus.isLowStock(stock)) {
                if (summary.length() > 0) {
                    summary.append("\n");
                }
                summary.append("• ").append(p.getName()).append(" — LOW STOCK (").append(stock).append(" left)");
            }
        }
        if (summary.length() > 0) {
            inventorySystem.publishEvent(ShopEaseAppEvents.ADMIN_LOGIN_STOCK, summary.toString());
        }
    }

    /** Compares saved stock snapshots with current stock — tells customer when wishlist items are back. */
    public void publishWishlistRestockOnLogin() {
        if (userWishlist == null || currentUser == null) {
            return;
        }
        StringBuilder restocked = new StringBuilder();
        for (Product p : userWishlist.getProducts(wishlistDAO, productDAO)) {
            int now = p.getStockQuantity();
            int lastKnown = wishlistDAO.getLastKnownStock(currentUser.getUserId(), p.getProductId());
            if (now > 0 && lastKnown == 0) {
                if (restocked.length() > 0) {
                    restocked.append("\n");
                }
                restocked.append("• ").append(p.getName()).append(" (").append(now).append(" in stock)");
            }
            wishlistDAO.saveStockSnapshot(currentUser.getUserId(), p.getProductId(), now);
        }
        if (restocked.length() > 0) {
            inventorySystem.publishEvent(ShopEaseAppEvents.WISHLIST_RESTOCK, restocked.toString());
        }
    }

    /** If the Singleton cart still has items, publish CART_REMINDER so the popup observer can nudge the user. */
    public void publishCartReminderIfNeeded() {
        syncCartWithDatabase();
        int count = getCartItemCount();
        if (count <= 0) {
            return;
        }
        String message = count == 1
                ? "You have 1 item waiting in your cart. Open View Cart to review or checkout."
                : "You have " + count + " items waiting in your cart. Open View Cart to review or checkout.";
        inventorySystem.publishEvent(ShopEaseAppEvents.CART_REMINDER, message);
    }

    /** Keeps cart line items aligned with current database stock and prices. */
    public void syncCartWithDatabase() {
        if (userCart == null) {
            return;
        }
        Iterator<CartItem> iterator = userCart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            Product fresh = productDAO.getProductById(item.getProduct().getProductId());
            if (fresh == null) {
                iterator.remove();
                continue;
            }
            item.getProduct().setStockQuantity(fresh.getStockQuantity());
            if (fresh.getStockQuantity() <= 0) {
                iterator.remove();
                lastMessage = fresh.getName() + " was removed from cart (out of stock).";
            } else if (item.getQuantity() > fresh.getStockQuantity()) {
                item.setQuantity(fresh.getStockQuantity());
                lastMessage = fresh.getName() + " quantity adjusted to available stock.";
            }
        }
    }

    /** Called after any write — tells DataChangeRefreshObserver to repaint open screens. */
    private void notifyDataChanged() {
        syncCartWithDatabase();
        inventorySystem.publishEvent(ShopEaseAppEvents.DATA_CHANGED, "");
    }

    public List<Product> getAllProducts() {
        syncCartWithDatabase();
        return productDAO.getAllProducts();
    }

    public List<Order> getOrderHistory() {
        if (currentUser == null) {
            return List.of();
        }
        return orderDAO.getOrdersByUserId(currentUser.getUserId());
    }

    public Product getProductById(String productId) {
        return productDAO.getProductById(productId);
    }

    public void updateProductStock(String productId, String productName, int newStock) {
        productDAO.updateStock(productId, Math.max(0, newStock));
        // Observer — setStock checks thresholds and may fire LOW_STOCK or OUT_OF_STOCK events
        inventorySystem.setStock(Math.max(0, newStock), productName);
    }

    public boolean restockProduct(String productId, int amountToAdd) {
        return runInventoryAction(restockStrategy, productId, amountToAdd, "");
    }

    public boolean reduceStockProduct(String productId, int amountToReduce, String remarks) {
        return runInventoryAction(reduceStrategy, productId, amountToReduce, remarks);
    }

    private boolean runInventoryAction(ShopEaseInventoryActionStrategy strategy,
                                       String productId, int amount, String remarks) {
        lastMessage = "";
        ShopEaseInventoryActionStrategy.InventoryActionResult result =
                new ShopEaseInventoryActionStrategy.InventoryActionResult();
        ShopEaseInventoryActionStrategy.InventoryActionRequest request =
                new ShopEaseInventoryActionStrategy.InventoryActionRequest(
                        productDAO,
                        adminActivityDAO,
                        inventorySystem,
                        currentUser,
                        productId,
                        amount,
                        remarks,
                        lastUndoableStockAction);
        ShopEaseInventoryActionContext actionContext = new ShopEaseInventoryActionContext(strategy);
        if (!actionContext.contextInterface(request, result)) {
            lastMessage = result.message;
            if (result.clearedUndoAction != null) {
                lastUndoableStockAction = null;
            }
            return false;
        }
        lastMessage = result.message;
        if (strategy instanceof UndoInventoryStrategy) {
            lastUndoableStockAction = null;
        } else if (result.newLog != null) {
            lastUndoableStockAction = result.newLog;
        }
        notifyDataChanged();
        return true;
    }

    public boolean canUndoLastRestock() {
        return lastUndoableStockAction != null && currentUser instanceof Admin;
    }

    public boolean undoLastRestock() {
        if (!canUndoLastRestock()) {
            lastMessage = "Nothing to undo.";
            return false;
        }
        return runInventoryAction(
                undoStrategy, lastUndoableStockAction.getProductId(), 0, "");
    }

    public List<AdminInventoryLog> getAdminActivityHistory() {
        if (currentUser instanceof Admin) {
            return adminActivityDAO.getLogsForAdmin(currentUser.getUserId(), 50);
        }
        return List.of();
    }

    // ==================== SHOPPING CART (uses ShopEaseCartSingleton) ====================

    public ShopEaseCartSingleton getCart() {
        return userCart;
    }

    public boolean addToCart(Product p, int qty) {
        lastMessage = "";
        if (userCart == null) {
            lastMessage = "Only customers can use the cart.";
            return false;
        }
        if (p == null || qty <= 0) {
            lastMessage = "Invalid product or quantity.";
            return false;
        }

        Product fresh = productDAO.getProductById(p.getProductId());
        if (fresh == null) {
            lastMessage = "Product not found.";
            return false;
        }
        if (fresh.getStockQuantity() <= 0) {
            lastMessage = fresh.getName() + " is out of stock.";
            return false;
        }

        int existingQty = 0;
        for (CartItem item : userCart.getItems()) {
            if (item.getProduct().getProductId().equals(fresh.getProductId())) {
                existingQty = item.getQuantity();
                break;
            }
        }
        int totalQty = existingQty + qty;
        if (totalQty > fresh.getStockQuantity()) {
            lastMessage = "Only " + fresh.getStockQuantity() + " available for " + fresh.getName() + ".";
            return false;
        }

        boolean merged = false;
        for (CartItem item : userCart.getItems()) {
            if (item.getProduct().getProductId().equals(fresh.getProductId())) {
                item.setQuantity(totalQty);
                item.getProduct().setStockQuantity(fresh.getStockQuantity());
                merged = true;
                break;
            }
        }
        if (!merged) {
            userCart.addItem(new CartItem(fresh, qty));
        }
        lastMessage = qty + "x " + fresh.getName() + " added to cart.";
        notifyDataChanged();
        return true;
    }

    public int getCartItemCount() {
        if (userCart == null) {
            return 0;
        }
        int count = 0;
        for (CartItem item : userCart.getItems()) {
            count += item.getQuantity();
        }
        return count;
    }

    public double getCartTotal() {
        if (userCart == null) {
            return 0;
        }
        double total = 0;
        for (CartItem item : userCart.getItems()) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        return total;
    }

    public boolean removeFromCart(String productId) {
        lastMessage = "";
        if (userCart == null) {
            return false;
        }
        boolean removed = userCart.getItems().removeIf(
                item -> item.getProduct().getProductId().equals(productId));
        if (removed) {
            lastMessage = "Item removed from cart.";
            notifyDataChanged();
        }
        return removed;
    }

    public void clearCart() {
        if (userCart != null) {
            userCart.getItems().clear();
            lastMessage = "Cart cleared.";
            notifyDataChanged();
        }
    }

    public boolean updateCartQuantity(String productId, int qty) {
        lastMessage = "";
        if (userCart == null) {
            return false;
        }
        if (qty <= 0) {
            return removeFromCart(productId);
        }
        Product fresh = productDAO.getProductById(productId);
        if (fresh == null) {
            lastMessage = "Product not found.";
            return false;
        }
        if (qty > fresh.getStockQuantity()) {
            lastMessage = "Only " + fresh.getStockQuantity() + " available.";
            return false;
        }
        for (CartItem item : userCart.getItems()) {
            if (item.getProduct().getProductId().equals(productId)) {
                item.setQuantity(qty);
                item.getProduct().setStockQuantity(fresh.getStockQuantity());
                notifyDataChanged();
                return true;
            }
        }
        lastMessage = "Item not in cart.";
        return false;
    }

    // ==================== CHECKOUT (Strategy pattern for payment method) ====================

    public boolean checkout(ShopEasePaymentStrategy strategy) {
        lastMessage = "";
        lastOrderId = "";
        if (userCart == null || userCart.getItems().isEmpty()) {
            lastMessage = "Your cart is empty.";
            return false;
        }

        List<CartItem> itemsToProcess = new ArrayList<>(userCart.getItems());
        double total = 0;

        for (CartItem item : itemsToProcess) {
            Product fresh = productDAO.getProductById(item.getProduct().getProductId());
            if (fresh == null) {
                lastMessage = "A product in your cart is no longer available.";
                return false;
            }
            if (item.getQuantity() > fresh.getStockQuantity()) {
                lastMessage = "Not enough stock for " + fresh.getName()
                        + " (only " + fresh.getStockQuantity() + " left).";
                return false;
            }
            total += fresh.getPrice() * item.getQuantity();
        }

        // Strategy — the UI already picked Credit Card / DuitNow / MAE / TNG; we just run it here
        ShopEasePaymentContext context = new ShopEasePaymentContext(strategy);
        context.contextInterface(total);

        // Save purchased items before clearing the cart
        List<CartItem> purchasedItems = new ArrayList<>();
        for (CartItem item : itemsToProcess) {
            Product fresh = productDAO.getProductById(item.getProduct().getProductId());
            purchasedItems.add(new CartItem(fresh, item.getQuantity()));
        }

        String orderId = OrderIdGenerator.nextOrderId(orderDAO);
        Order order = new Order(orderId, total);
        order.setStatus("Completed");
        order.setItems(purchasedItems);
        orderDAO.insertOrder(order, currentUser.getUserId());

        userCart.getItems().clear();

        for (CartItem item : purchasedItems) {
            Product prod = item.getProduct();
            int newStock = prod.getStockQuantity() - item.getQuantity();
            updateProductStock(prod.getProductId(), prod.getName(), newStock);
        }

        lastOrderId = orderId;
        lastMessage = "Order " + orderId + " placed successfully.";
        notifyDataChanged();
        return true;
    }

    // ==================== WISHLIST (uses ShopEaseWishlistSingleton + database) ====================
    public boolean addToWishlist(String productId) {
        lastMessage = "";
        if (!(currentUser instanceof Customer)) {
            lastMessage = "Wishlist is for customer accounts only.";
            return false;
        }
        if (productDAO.getProductById(productId) == null) {
            lastMessage = "Product not found.";
            return false;
        }
        if (userWishlist.add(wishlistDAO, productId)) {
            Product p = productDAO.getProductById(productId);
            if (p != null) {
                wishlistDAO.saveStockSnapshot(currentUser.getUserId(), productId, p.getStockQuantity());
            }
            lastMessage = "Added to wishlist.";
            notifyDataChanged();
            return true;
        }
        lastMessage = "Already in wishlist or product not found.";
        return false;
    }

    public boolean removeFromWishlist(String productId) {
        if (currentUser == null) {
            return false;
        }
        boolean removed = userWishlist != null && userWishlist.remove(wishlistDAO, productId);
        if (removed) {
            lastMessage = "Removed from wishlist.";
            notifyDataChanged();
        }
        return removed;
    }

    public boolean isInWishlist(String productId) {
        if (currentUser == null) {
            return false;
        }
        return userWishlist != null && userWishlist.contains(wishlistDAO, productId);
    }

    public List<Product> getWishlistProducts() {
        if (!(currentUser instanceof Customer)) {
            return List.of();
        }
        return userWishlist.getProducts(wishlistDAO, productDAO);
    }

    public boolean moveWishlistItemToCart(String productId, int qty) {
        Product p = productDAO.getProductById(productId);
        if (p == null) {
            lastMessage = "Product not found.";
            return false;
        }
        if (addToCart(p, qty)) {
            removeFromWishlist(productId);
            lastMessage = "Moved to cart.";
            return true;
        }
        return false;
    }

    // ==================== PROFILE (Strategy — CustomerProfileUpdateStrategy) ====================

    public boolean updateCustomerProfile(String name, String email, String newPassword) {
        lastMessage = "";
        ShopEaseProfileUpdateStrategy.ProfileUpdateResult result =
                new ShopEaseProfileUpdateStrategy.ProfileUpdateResult();
        if (!profileUpdateStrategy.update(userDAO, currentUser, name, email, newPassword, result)) {
            lastMessage = result.message;
            return false;
        }
        currentUser = result.updatedUser;
        lastMessage = result.message;
        notifyDataChanged();
        return true;
    }

    // ==================== ADMIN USER CRUD (Strategy — Create / Update / Delete) ====================

    public List<User> getAllUsersForAdmin() {
        if (!(currentUser instanceof Admin)) {
            return List.of();
        }
        return userDAO.getAllUsers().stream()
                .filter(u -> u instanceof Customer)
                .toList();
    }

    public List<Order> getOrdersForUserAsAdmin(String userId) {
        if (!(currentUser instanceof Admin)) {
            return List.of();
        }
        return orderDAO.getOrdersByUserId(userId);
    }

    public List<Product> getWishlistForUserAsAdmin(String userId) {
        if (!(currentUser instanceof Admin)) {
            return List.of();
        }
        return wishlistDAO.getProductsForUser(userId, productDAO);
    }

    public boolean createCustomerAsAdmin(String name, String email, String password) {
        lastMessage = "";
        ShopEaseAdminUserActionStrategy.AdminUserActionResult result =
                new ShopEaseAdminUserActionStrategy.AdminUserActionResult();
        if (!createUserStrategy.create(userDAO, currentUser, name, email, password, result)) {
            lastMessage = result.message;
            return false;
        }
        lastMessage = result.message;
        notifyDataChanged();
        return true;
    }

    public boolean updateCustomerAsAdmin(String userId, String name, String email, String password) {
        lastMessage = "";
        ShopEaseAdminUserActionStrategy.AdminUserActionResult result =
                new ShopEaseAdminUserActionStrategy.AdminUserActionResult();
        if (!updateUserStrategy.update(userDAO, currentUser, userId, name, email, password, result)) {
            lastMessage = result.message;
            return false;
        }
        lastMessage = result.message;
        notifyDataChanged();
        return true;
    }

    public boolean deleteUserAsAdmin(String userId) {
        lastMessage = "";
        ShopEaseAdminUserActionStrategy.AdminUserActionResult result =
                new ShopEaseAdminUserActionStrategy.AdminUserActionResult();
        if (!deleteUserStrategy.execute(userDAO, currentUser, userId, result)) {
            lastMessage = result.message;
            return false;
        }
        lastMessage = result.message;
        notifyDataChanged();
        return true;
    }
}
