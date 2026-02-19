package com.pos;

/**
 * Represents a product in the POS system
 */
public class Product {
    private String id;
    private String name;
    private double price;
    private int quantity;

    public Product(String id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isAvailable(int requestedQuantity) {
        return quantity >= requestedQuantity;
    }

    @Override
    public String toString() {
        return String.format("Product{id='%s', name='%s', price=%.2f, quantity=%d}", 
                id, name, price, quantity);
    }
}
