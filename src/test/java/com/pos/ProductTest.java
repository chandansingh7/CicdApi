package com.pos;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProductCreation() {
        Product product = new Product("P001", "Laptop", 999.99, 10);
        assertEquals("P001", product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals(999.99, product.getPrice());
        assertEquals(10, product.getQuantity());
    }

    @Test
    void testIsAvailable() {
        Product product = new Product("P001", "Laptop", 999.99, 10);
        assertTrue(product.isAvailable(5));
        assertTrue(product.isAvailable(10));
        assertFalse(product.isAvailable(11));
    }

    @Test
    void testSetQuantity() {
        Product product = new Product("P001", "Laptop", 999.99, 10);
        product.setQuantity(5);
        assertEquals(5, product.getQuantity());
    }
}
