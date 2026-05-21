package com.shopease.service;

import com.shopease.dao.AdminActivityDAO;
import com.shopease.dao.DatabaseConnection;
import com.shopease.dao.OrderDAO;
import com.shopease.dao.ProductDAO;
import com.shopease.dao.UserDAO;
import com.shopease.model.*;
import com.shopease.observer.*;
import com.shopease.singleton.ShopEaseCartSingleton;
import com.shopease.strategy.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShopEaseService {
    private ProductDAO productDAO;
    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private AdminActivityDAO adminActivityDAO;

    private User currentUser;
    private ShopEaseCartSingleton userCart;
    private ShopEaseInventorySubject inventorySystem;
    private String lastMessage = "";
    private AdminInventoryLog lastUndoableStockAction;
    private final List<DataChangeListener> dataChangeListeners = new CopyOnWriteArrayList<>();

    public ShopEaseService() {
        DatabaseConnection.initializeDatabase();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
        this.orderDAO = new OrderDAO();
        this.adminActivityDAO = new AdminActivityDAO();
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

    public boolean login(String email, String password) {
        lastMessage = "";
        if (email == null || password == null) {
            lastMessage = "Email and password are required.";
            return false;
        }
        String trimmedEmail = email.trim();
        User user = userDAO.getUserByEmail(trimmedEmail);
        if (user != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            this.inventorySystem = new ShopEaseInventorySubject();
            this.userCart = null;
            this.lastUndoableStockAction = null;

            if (user instanceof Customer) {
                userCart = ShopEaseCartSingleton.getInstance(user.getUserId());
                inventorySystem.attach(new ShopEaseShoppingCartObserver(userCart));
            }
            inventorySystem.attach(new ShopEaseAdminObserver());
            return true;
        }
        lastMessage = "Invalid email or password.";
        return false;
    }

    public boolean registerCustomer(User customer) {
        lastMessage = "";
        if (customer == null || customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            lastMessage = "Email is required.";
            return false;
        }
        if (userDAO.getUserByEmail(customer.getEmail().trim()) != null) {
            lastMessage = "An account with this email already exists.";
            return false;
        }
        if (userDAO.getUserById(customer.getUserId()) != null) {
            lastMessage = "User ID already exists. Please try again.";
            return false;
        }
        if (!userDAO.insertUser(customer)) {
            lastMessage = "Registration failed. Please try again.";
            return false;
        }
        return true;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void logout() {
        this.currentUser = null;
        this.userCart = null;
        this.inventorySystem = new ShopEaseInventorySubject();
        this.lastMessage = "";
        this.lastUndoableStockAction = null;
        dataChangeListeners.clear();
    }

    public void addDataChangeListener(DataChangeListener listener) {
        if (listener != null) {
            dataChangeListeners.add(listener);
        }
    }

    public void removeDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.remove(listener);
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

    private void notifyDataChanged() {
        syncCartWithDatabase();
        for (DataChangeListener listener : dataChangeListeners) {
            javax.swing.SwingUtilities.invokeLater(listener::onDataChanged);
        }
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
        inventorySystem.setStock(Math.max(0, newStock), productName);
    }

    public boolean restockProduct(String productId, int amountToAdd) {
        return adjustAdminStock(productId, amountToAdd, "RESTOCK", "");
    }

    public boolean reduceStockProduct(String productId, int amountToReduce, String remarks) {
        lastMessage = "";
        if (remarks == null || remarks.trim().isEmpty()) {
            lastMessage = "Remarks are required when reducing stock.";
            return false;
        }
        if (amountToReduce <= 0) {
            lastMessage = "Reduce amount must be greater than 0.";
            return false;
        }
        return adjustAdminStock(productId, -amountToReduce, "REDUCE", remarks.trim());
    }

    private boolean adjustAdminStock(String productId, int quantityChange, String actionType, String remarks) {
        lastMessage = "";
        if (!(currentUser instanceof Admin)) {
            lastMessage = "Only admins can change stock.";
            return false;
        }
        if (quantityChange == 0) {
            lastMessage = "Change amount cannot be zero.";
            return false;
        }
        Product product = productDAO.getProductById(productId);
        if (product == null) {
            lastMessage = "Product not found.";
            return false;
        }

        int stockBefore = product.getStockQuantity();
        int stockAfter = Math.max(0, stockBefore + quantityChange);
        if (actionType.equals("REDUCE") && quantityChange < 0 && stockAfter == stockBefore) {
            lastMessage = "Stock is already 0 for " + product.getName() + ".";
            return false;
        }

        updateProductStock(productId, product.getName(), stockAfter);

        AdminInventoryLog log = new AdminInventoryLog(
                currentUser.getUserId(),
                currentUser.getName(),
                productId,
                product.getName(),
                actionType,
                stockAfter - stockBefore,
                stockBefore,
                stockAfter,
                remarks,
                new java.util.Date()
        );
        adminActivityDAO.insertLog(log);
        lastUndoableStockAction = log;

        if ("REDUCE".equals(actionType)) {
            lastMessage = "Reduced " + product.getName() + " by " + (-quantityChange)
                    + " (now " + stockAfter + "). Remark saved.";
        } else {
            lastMessage = "Restocked " + product.getName() + " by +" + quantityChange
                    + " (now " + stockAfter + ").";
        }
        notifyDataChanged();
        return true;
    }

    public boolean canUndoLastRestock() {
        return lastUndoableStockAction != null && currentUser instanceof Admin;
    }

    public boolean undoLastRestock() {
        lastMessage = "";
        if (!canUndoLastRestock()) {
            lastMessage = "Nothing to undo.";
            return false;
        }

        AdminInventoryLog previous = lastUndoableStockAction;
        Product product = productDAO.getProductById(previous.getProductId());
        if (product == null) {
            lastMessage = "Product no longer exists.";
            lastUndoableStockAction = null;
            return false;
        }

        int currentStock = product.getStockQuantity();
        int restoredStock = previous.getStockBefore();
        updateProductStock(previous.getProductId(), previous.getProductName(), restoredStock);

        AdminInventoryLog undoLog = new AdminInventoryLog(
                currentUser.getUserId(),
                currentUser.getName(),
                previous.getProductId(),
                previous.getProductName(),
                "UNDO",
                restoredStock - currentStock,
                currentStock,
                restoredStock,
                "Undo " + previous.getActionType() + (previous.getRemarks().isEmpty() ? "" : ": " + previous.getRemarks()),
                new java.util.Date()
        );
        adminActivityDAO.insertLog(undoLog);
        lastUndoableStockAction = null;
        lastMessage = "Undid " + previous.getActionType().toLowerCase() + " on "
                + previous.getProductName() + " (back to " + restoredStock + ").";
        notifyDataChanged();
        return true;
    }

    public List<AdminInventoryLog> getAdminActivityHistory() {
        if (currentUser instanceof Admin) {
            return adminActivityDAO.getLogsForAdmin(currentUser.getUserId(), 50);
        }
        return List.of();
    }

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

    public boolean checkout(ShopEasePaymentStrategy strategy) {
        lastMessage = "";
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

        ShopEasePaymentContext context = new ShopEasePaymentContext(strategy);
        context.executeStrategy(total);

        // Save purchased items before clearing the cart
        List<CartItem> purchasedItems = new ArrayList<>();
        for (CartItem item : itemsToProcess) {
            Product fresh = productDAO.getProductById(item.getProduct().getProductId());
            purchasedItems.add(new CartItem(fresh, item.getQuantity()));
        }

        String orderId = "ORD-" + System.currentTimeMillis();
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

        lastMessage = "Order " + orderId + " placed successfully.";
        notifyDataChanged();
        return true;
    }
}
