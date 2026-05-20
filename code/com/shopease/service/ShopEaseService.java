package com.shopease.service;

import com.shopease.dao.DatabaseConnection;
import com.shopease.dao.ProductDAO;
import com.shopease.dao.UserDAO;
import com.shopease.model.*;
import com.shopease.observer.*;
import com.shopease.singleton.ShopEaseCartSingleton;
import com.shopease.strategy.*;

import java.util.List;

public class ShopEaseService {
    private ProductDAO productDAO;
    private UserDAO userDAO;
    
    private User currentUser;
    private ShopEaseCartSingleton userCart;
    private ShopEaseInventorySubject inventorySystem;

    public ShopEaseService() {
        DatabaseConnection.initializeDatabase();
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
        this.inventorySystem = new ShopEaseInventorySubject();
        seedInitialData();
    }

    private void seedInitialData() {
        if (userDAO.getUserByEmail("admin@email.admin.my") == null) {
            userDAO.insertUser(new com.shopease.model.Admin("ADMIN-001", "Admin", "admin@email.admin.my", "adminpass"));
        }
        if (productDAO.getAllProducts().isEmpty()) {
            productDAO.insertProduct(new Product("LPT-01", "Gaming Laptop", 2500.00, 5, "Electronics"));
            productDAO.insertProduct(new Product("MSE-02", "Wireless Mouse", 50.00, 20, "Accessories"));
            productDAO.insertProduct(new Product("KBD-03", "Mechanical Keyboard", 150.00, 10, "Accessories"));
        }
    }

    public boolean login(String email, String password) {
        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            this.currentUser = user;
            
            // Set up Design Patterns
            if (user instanceof Customer) {
                userCart = ShopEaseCartSingleton.getInstance(user.getUserId());
                ShopEaseShoppingCartObserver cartObserver = new ShopEaseShoppingCartObserver(userCart);
                inventorySystem.attach(cartObserver);
            }
            
            ShopEaseAdminObserver adminObserver = new ShopEaseAdminObserver();
            inventorySystem.attach(adminObserver);
            
            return true;
        }
        return false;
    }

    public void registerCustomer(User customer) {
        userDAO.insertUser(customer);
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
    public void logout() {
        this.currentUser = null;
        this.userCart = null;
        this.inventorySystem = new ShopEaseInventorySubject(); // reset observers
    }

    public List<Product> getAllProducts() {
        return productDAO.getAllProducts();
    }

    public void updateProductStock(String productId, String productName, int newStock) {
        productDAO.updateStock(productId, newStock);
        // Trigger Observer Pattern
        inventorySystem.setStock(newStock, productName);
    }
    
    // Singleton Cart Integration
    public ShopEaseCartSingleton getCart() {
        return userCart;
    }
    
    public void addToCart(Product p, int qty) {
        if (userCart != null) {
            userCart.addItem(new CartItem(p, qty));
        }
    }
    
    // Strategy Pattern Checkout Integration
    public boolean checkout(ShopEasePaymentStrategy strategy) {
        if (userCart == null || userCart.getItems().isEmpty()) {
            return false;
        }
        
        double total = 0;
        for (CartItem item : userCart.getItems()) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }
        
        // Execute Strategy
        ShopEasePaymentContext context = new ShopEasePaymentContext(strategy);
        context.executeStrategy(total);
        
        // Update Inventory and DB (copy list to avoid ConcurrentModificationException from observers)
        java.util.List<CartItem> itemsToProcess = new java.util.ArrayList<>(userCart.getItems());
        for (CartItem item : itemsToProcess) {
            Product prod = item.getProduct();
            int newStock = prod.getStockQuantity() - item.getQuantity();
            updateProductStock(prod.getProductId(), prod.getName(), newStock);
        }
        
        // Clear cart
        userCart.getItems().clear();
        return true;
    }
}
