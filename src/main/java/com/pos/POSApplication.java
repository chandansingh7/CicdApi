package com.pos;

/**
 * Main application entry point
 */
public class POSApplication {
    public static void main(String[] args) {
        System.out.println("=== POS System Started ===");
        
        POSSystem pos = new POSSystem();
        
        // Demo transaction
        System.out.println("\n--- Adding items to cart ---");
        pos.addToCart("P001", 1); // Laptop
        pos.addToCart("P002", 2); // Mouse
        pos.addToCart("P003", 1); // Keyboard
        
        System.out.println("\n--- Cart Contents ---");
        Cart cart = pos.getCurrentCart();
        for (CartItem item : cart.getItems()) {
            System.out.println(item);
        }
        
        System.out.println("\n--- Checkout ---");
        double total = pos.checkout();
        System.out.println("Total: $" + String.format("%.2f", total));
        
        System.out.println("\n=== Transaction Complete ===");
    }
}
