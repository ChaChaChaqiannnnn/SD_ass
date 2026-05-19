package com.shopease;

import com.shopease.model.*;
import com.shopease.observer.*;
import com.shopease.strategy.*;
import com.shopease.singleton.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main entry point for the ShopEase E-Commerce System.
 * Demonstrates Observer, Strategy, and Singleton design patterns
 * within a localized Malaysian context.
 *
 * @author Group 5
 * @version 2.0
 */
public class Main {
    private static List<Product> productCatalog = new ArrayList<>();
    private static ShopEaseInventorySubject inventorySystem = new ShopEaseInventorySubject();
    private static Scanner scanner = new Scanner(System.in);
    private static User currentUser;
    private static ShopEaseCartSingleton userCart;

    private static List<Order> orderHistory = new ArrayList<>();
    private static List<Customer> registeredCustomers = new ArrayList<>();
    private static Admin defaultAdmin = new Admin("ADMIN-001", "Admin", "admin@email.admin.my", "adminpass");

    public static void main(String[] args) {
        setupInitialData();

        System.out.println("==================================================");
        System.out.println("   WELCOME TO SHOPEASE MALAYSIA                   ");
        System.out.println("==================================================");

        while (true) {
            System.out.println("\n[1] Register (Customer)");
            System.out.println("[2] Login");
            System.out.println("[3] Exit Program");
            System.out.print("Selection: ");
            String startChoice = scanner.nextLine();

            if (startChoice.equals("1")) {
                registerCustomer();
            } else if (startChoice.equals("2")) {
                if (loginUser()) {
                    runAppLoop();
                }
            } else if (startChoice.equals("3")) {
                System.out.println("Goodbye!");
                break;
            } else {
                System.out.println("Invalid selection.");
            }
        }
        scanner.close();
    }

    private static void runAppLoop() {
        boolean loggedIn = true;
        while (loggedIn) {
            printMenu();
            String choice = scanner.nextLine();
            loggedIn = handleChoice(choice);
        }
    }

    /**
     * Register a new customer account.
     */
    private static void registerCustomer() {
        System.out.println("\n--- CUSTOMER REGISTRATION ---");
        System.out.print("Enter your Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();

        if (!email.contains("@") || !email.contains(".com")) {
            System.out.println(">> Error: Invalid email format. Email must contain '@' and '.com'.");
            return;
        }

        for (Customer c : registeredCustomers) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                System.out.println(">> Error: This email is already registered. Please login instead.");
                return;
            }
        }

        String normalizedId = name.trim().toUpperCase().replaceAll("\\s+", "_");
        Customer newCustomer = new Customer("CUST-" + normalizedId, name, email, "pass123");
        registeredCustomers.add(newCustomer);
        System.out.println("\n>> Registration Successful! You can now login with your email.");
    }

    /**
     * Login for both customers and admin.
     */
    private static boolean loginUser() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter your Email: ");
        String email = scanner.nextLine();

        if (email.contains(".admin")) {
            if (email.equalsIgnoreCase(defaultAdmin.getEmail())) {
                currentUser = defaultAdmin;
                setupObservers();
                System.out.println("\n>> Success! Logged in as Admin: " + currentUser.getName());
                return true;
            } else {
                System.out.println(">> Error: Invalid admin credentials.");
                return false;
            }
        }

        for (Customer c : registeredCustomers) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                currentUser = c;
                userCart = ShopEaseCartSingleton.getInstance(currentUser.getUserId());
                setupObservers();
                System.out.println("\n>> Success! Logged in as: " + currentUser.getName());
                return true;
            }
        }

        System.out.println(">> Error: Email not found. Please register first.");
        return false;
    }

    /**
     * Attach ShopEase observers to the inventory subject for the current session.
     */
    private static void setupObservers() {
        if (currentUser instanceof Customer) {
            userCart = ShopEaseCartSingleton.getInstance(currentUser.getUserId());
            ShopEaseShoppingCartObserver cartObserver = new ShopEaseShoppingCartObserver(userCart);
            inventorySystem.attach(cartObserver);
        }
        ShopEaseAdminObserver adminObserver = new ShopEaseAdminObserver();
        inventorySystem.attach(adminObserver);
    }

    private static void setupInitialData() {
        productCatalog.add(new Product("LPT-01", "Gaming Laptop", 2500.00, 5, "Electronics"));
        productCatalog.add(new Product("MSE-02", "Wireless Mouse", 50.00, 20, "Accessories"));
        productCatalog.add(new Product("KBD-03", "Mechanical Keyboard", 150.00, 10, "Accessories"));
    }

    private static void printMenu() {
        System.out.println("\n--- MAIN MENU (" + currentUser.getClass().getSimpleName() + ") ---");
        if (currentUser instanceof Customer) {
            System.out.println("1. View/Search Products");
            System.out.println("2. Add Item to Cart");
            System.out.println("3. View/Update Cart");
            System.out.println("4. Checkout");
            System.out.println("5. View Order History");
            System.out.println("6. Logout");
        } else {
            System.out.println("1. View Products");
            System.out.println("2. Manage Inventory");
            System.out.println("3. Logout");
        }
        System.out.print("Selection: ");
    }

    public static boolean handleChoice(String choice) {
        if (currentUser instanceof Customer) {
            switch (choice) {
                case "1": viewProductsWithSearch(); break;
                case "2": addToCart(); break;
                case "3": viewAndModifyCart(); break;
                case "4": checkout(); break;
                case "5": viewOrderHistory(); break;
                case "6": return false;
                default: System.out.println("Invalid choice.");
            }
        } else {
            switch (choice) {
                case "1": viewProducts(); break;
                case "2": manageInventory(); break;
                case "3": return false;
                default: System.out.println("Invalid choice.");
            }
        }
        return true;
    }

    private static void viewProducts() {
        System.out.println("\n--- PRODUCT CATALOG ---");
        for (int i = 0; i < productCatalog.size(); i++) {
            Product p = productCatalog.get(i);
            System.out.println((i + 1) + ". " + p.getName() + " - RM" + p.getPrice() + " [Stock: " + p.getStockQuantity() + "]");
        }
    }

    private static void viewProductsWithSearch() {
        System.out.println("\n0. Return");
        System.out.println("1. View All Products");
        System.out.println("2. Search Product by Name");
        System.out.print("Choice: ");
        String sub = scanner.nextLine();

        if (sub.equals("0")) {
            return;
        } else if (sub.equals("2")) {
            System.out.print("Enter search term: ");
            String term = scanner.nextLine().toLowerCase();
            System.out.println("\n--- SEARCH RESULTS ---");
            for (Product p : productCatalog) {
                if (p.getName().toLowerCase().contains(term)) {
                    System.out.println("- " + p.getName() + " (RM" + p.getPrice() + ")");
                }
            }
        } else {
            viewProducts();
        }
    }

    private static void addToCart() {
        viewProducts();
        System.out.println("0. Return");
        System.out.print("Select product number: ");
        try {
            String input = scanner.nextLine();
            if (input.equals("0")) return;
            int index = Integer.parseInt(input) - 1;
            Product p = productCatalog.get(index);
            System.out.print("Enter quantity (0 to cancel): ");
            int qty = Integer.parseInt(scanner.nextLine());
            if (qty == 0) return;

            if (qty <= p.getStockQuantity()) {
                userCart.addItem(new CartItem(p, qty));
                System.out.println(">> Success: Added to cart.");
            } else {
                System.out.println(">> Error: Not enough stock.");
            }
        } catch (Exception e) { System.out.println("Invalid input."); }
    }

    private static void viewAndModifyCart() {
        System.out.println("\n--- SHOPPING CART ---");
        if (userCart.getItems().isEmpty()) {
            System.out.println("Empty.");
            return;
        }
        for (int i = 0; i < userCart.getItems().size(); i++) {
            CartItem item = userCart.getItems().get(i);
            System.out.println((i + 1) + ". " + item.getProduct().getName() + " x " + item.getQuantity());
        }
        System.out.println("\n0. Return");
        System.out.println("1. Continue");
        System.out.println("2. Update Quantity");
        System.out.print("Choice: ");
        String cartChoice = scanner.nextLine();
        if (cartChoice.equals("0")) {
            return;
        } else if (cartChoice.equals("2")) {
            System.out.print("Enter item number (0 to cancel): ");
            int idx = Integer.parseInt(scanner.nextLine());
            if (idx == 0) return;
            idx = idx - 1;
            if (idx < 0 || idx >= userCart.getItems().size()) {
                System.out.println(">> Error: Invalid item number.");
                return;
            }
            System.out.print("Enter new quantity: ");
            int newQty = Integer.parseInt(scanner.nextLine());
            if (newQty <= 0) {
                System.out.println(">> Error: Quantity must be greater than 0.");
                return;
            }
            CartItem item = userCart.getItems().get(idx);
            if (newQty > item.getProduct().getStockQuantity()) {
                System.out.println(">> Error: Not enough stock available.");
                return;
            }
            item.setQuantity(newQty);
            System.out.println(">> Updated.");
        }
    }

    private static void viewOrderHistory() {
        System.out.println("\n--- YOUR ORDER HISTORY ---");
        if (orderHistory.isEmpty()) System.out.println("No past orders.");
        for (Order o : orderHistory) {
            System.out.println("- Order " + o.getOrderId() + " | Amount: RM" + o.getTotalAmount() + " | Status: " + o.getStatus());
        }
    }

    private static void manageInventory() {
        viewProducts();
        System.out.println("0. Return");
        System.out.print("Product to update: ");
        try {
            String input = scanner.nextLine();
            if (input.equals("0")) return;
            int index = Integer.parseInt(input) - 1;
            Product p = productCatalog.get(index);
            System.out.print("New stock level: ");
            int newStock = Integer.parseInt(scanner.nextLine());

            p.setStockQuantity(newStock);
            System.out.println("\n--- ADMIN ACTION: UPDATING STOCK ---");
            inventorySystem.setStock(newStock, p.getName());
        } catch (Exception e) { System.out.println("Invalid input."); }
    }

    /**
     * Checkout using the ShopEase Strategy Design Pattern.
     * ShopEasePaymentContext delegates to the selected ShopEasePaymentStrategy.
     */
    private static void checkout() {
        if (userCart.getItems().isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        double total = 0;
        for (CartItem item : userCart.getItems()) {
            total += item.getProduct().getPrice() * item.getQuantity();
        }

        System.out.println("\n--- CHECKOUT ---");
        System.out.println("Total Amount: RM" + String.format("%.2f", total));
        System.out.println("Select Payment Method:");
        System.out.println("0. Return (Cancel Checkout)");
        System.out.println("1. Credit Card");
        System.out.println("2. DuitNow QR");
        System.out.println("3. MAE Banking App");
        System.out.println("4. Touch 'n Go e-Wallet");
        System.out.print("Selection: ");
        String type = scanner.nextLine();

        if (type.equals("0")) {
            System.out.println(">> Checkout cancelled.");
            return;
        }

        try {
            // Strategy Pattern: select a ShopEasePaymentStrategy based on user input
            ShopEasePaymentStrategy selectedStrategy;
            switch (type) {
                case "1": selectedStrategy = new ShopEaseCreditCardStrategy(); break;
                case "2": selectedStrategy = new ShopEaseDuitNowStrategy();    break;
                case "3": selectedStrategy = new ShopEaseMAEStrategy();        break;
                case "4": selectedStrategy = new ShopEaseTNGStrategy();        break;
                default:
                    System.out.println(">> Invalid payment method.");
                    return;
            }

            // ShopEasePaymentContext holds the strategy and delegates execution
            ShopEasePaymentContext context = new ShopEasePaymentContext(selectedStrategy);
            System.out.println("\n--- Processing Transaction ---");
            context.executeStrategy(total);

            String orderId = "ORD-" + (int)(Math.random() * 10000);
            Order order = new Order(orderId, total);
            order.setStatus("Completed");
            orderHistory.add(order);

            System.out.println(">> SUCCESS: Order [" + order.getOrderId() + "] Completed.");

            List<CartItem> purchasedItems = new ArrayList<>(userCart.getItems());
            userCart.getItems().clear();

            for (CartItem item : purchasedItems) {
                Product prod = item.getProduct();
                int newStock = prod.getStockQuantity() - item.getQuantity();
                prod.setStockQuantity(newStock);
                inventorySystem.setStock(newStock, prod.getName());
            }
        } catch (Exception e) { System.out.println(">> Checkout Failed: " + e.getMessage()); }
    }
}
