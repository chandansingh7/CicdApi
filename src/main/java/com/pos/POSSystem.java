package com.pos;

import java.util.HashMap;
import java.util.Map;

/**
 * Main Point of Sale System
 */
public class POSSystem {
    private Map<String, Product> inventory;
    private Cart currentCart;

    public POSSystem() {
        this.inventory = new HashMap<>();
        this.currentCart = new Cart();
        initializeInventory();
    }

    private void initializeInventory() {
        // Initialize with some sample products
        addProduct(new Product("P001", "Laptop", 999.99, 10));
        addProduct(new Product("P002", "Mouse", 29.99, 50));
        addProduct(new Product("P003", "Keyboard", 79.99, 30));
        addProduct(new Product("P004", "Monitor", 299.99, 15));
    }

    public void addProduct(Product product) {
        inventory.put(product.getId(), product);
    }

    public Product getProduct(String productId) {
        return inventory.get(productId);
    }

    public void addToCart(String productId, int quantity) {
        Product product = inventory.get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }
        currentCart.addItem(product, quantity);
    }

    public void removeFromCart(String productId) {
        currentCart.removeItem(productId);
    }

    public Cart getCurrentCart() {
        return currentCart;
    }

    public double checkout() {
        if (currentCart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        double total = currentCart.getTotal();
        
        // Update inventory
        for (CartItem item : currentCart.getItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
        }

        // Clear cart after checkout
        currentCart.clear();
        
        return total;
    }

    public void startNewTransaction() {
        currentCart.clear();
    }

    public Map<String, Product> getInventory() {
        return new HashMap<>(inventory);
    }
}
