package com.pos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class POSSystemTest {

    private POSSystem pos;

    @BeforeEach
    void setUp() {
        pos = new POSSystem();
    }

    @Test
    void testAddToCart() {
        pos.addToCart("P001", 1);
        Cart cart = pos.getCurrentCart();
        assertEquals(1, cart.getItemCount());
    }

    @Test
    void testCheckout() {
        pos.addToCart("P001", 1); // Laptop: 999.99
        pos.addToCart("P002", 2); // Mouse: 29.99 * 2 = 59.98
        
        double total = pos.checkout();
        assertEquals(1059.97, total, 0.01);
        assertTrue(pos.getCurrentCart().isEmpty());
    }

    @Test
    void testCheckoutEmptyCart() {
        assertThrows(IllegalStateException.class, () -> {
            pos.checkout();
        });
    }

    @Test
    void testProductNotFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            pos.addToCart("INVALID", 1);
        });
    }

    @Test
    void testInventoryUpdateAfterCheckout() {
        Product product = pos.getProduct("P001");
        int initialQuantity = product.getQuantity();
        
        pos.addToCart("P001", 2);
        pos.checkout();
        
        assertEquals(initialQuantity - 2, product.getQuantity());
    }

    @Test
    void testStartNewTransaction() {
        pos.addToCart("P001", 1);
        pos.startNewTransaction();
        assertTrue(pos.getCurrentCart().isEmpty());
    }

    @Test
    void testGetInventory() {
        assertNotNull(pos.getInventory());
        assertTrue(pos.getInventory().containsKey("P001"));
    }
}
