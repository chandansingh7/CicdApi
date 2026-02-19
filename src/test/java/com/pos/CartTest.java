package com.pos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CartTest {

    private Cart cart;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        product1 = new Product("P001", "Laptop", 999.99, 10);
        product2 = new Product("P002", "Mouse", 29.99, 50);
    }

    @Test
    void testAddItem() {
        cart.addItem(product1, 2);
        assertEquals(1, cart.getItemCount());
        assertEquals(1999.98, cart.getTotal(), 0.01);
    }

    @Test
    void testAddMultipleItems() {
        cart.addItem(product1, 1);
        cart.addItem(product2, 3);
        assertEquals(2, cart.getItemCount());
        assertEquals(1089.96, cart.getTotal(), 0.01);
    }

    @Test
    void testAddSameProductMultipleTimes() {
        cart.addItem(product1, 2);
        cart.addItem(product1, 3);
        assertEquals(1, cart.getItemCount());
        assertEquals(4999.95, cart.getTotal(), 0.01);
    }

    @Test
    void testRemoveItem() {
        cart.addItem(product1, 1);
        cart.addItem(product2, 1);
        cart.removeItem("P001");
        assertEquals(1, cart.getItemCount());
        assertEquals(29.99, cart.getTotal(), 0.01);
    }

    @Test
    void testClear() {
        cart.addItem(product1, 1);
        cart.addItem(product2, 1);
        cart.clear();
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotal(), 0.01);
    }

    @Test
    void testInsufficientStock() {
        assertThrows(IllegalArgumentException.class, () -> {
            cart.addItem(product1, 11);
        });
    }

    @Test
    void testIsEmpty() {
        assertTrue(cart.isEmpty());
        cart.addItem(product1, 1);
        assertFalse(cart.isEmpty());
    }
}
