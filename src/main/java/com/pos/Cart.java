package com.pos;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shopping cart
 */
public class Cart {
    private List<CartItem> items;

    public Cart() {
        this.items = new ArrayList<>();
    }

    public void addItem(Product product, int quantity) {
        if (!product.isAvailable(quantity)) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        // Check if product already exists in cart
        for (CartItem item : items) {
            if (item.getProduct().getId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }

        items.add(new CartItem(product, quantity));
    }

    public void removeItem(String productId) {
        items.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getTotal() {
        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public int getItemCount() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
