package com.pos.config;

import com.pos.entity.Category;
import com.pos.entity.Inventory;
import com.pos.entity.Product;
import com.pos.entity.User;
import com.pos.enums.Role;
import com.pos.repository.CategoryRepository;
import com.pos.repository.InventoryRepository;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository     userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;
    private final InventoryRepository inventoryRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByUsername("admin")) {
            log.info("Seed data already present — skipping DataInitializer.");
            return;
        }

        log.info("=== Seeding initial data ===");
        seedUsers();
        List<Category> cats = seedCategories();
        seedProductsAndInventory(cats);
        log.info("=== Seed complete ===");
    }

    // ── Users ────────────────────────────────────────────────────────────────

    private void seedUsers() {
        userRepository.saveAll(List.of(
            user("admin",    "admin@pos.com",    "Admin@1234",    Role.ADMIN),
            user("manager1", "manager@pos.com",  "Manager@1234",  Role.MANAGER),
            user("cashier1", "cashier1@pos.com", "Cashier@1234",  Role.CASHIER),
            user("cashier2", "cashier2@pos.com", "Cashier@1234",  Role.CASHIER)
        ));
        log.info("Seeded 4 users (admin / manager1 / cashier1 / cashier2)");
    }

    private User user(String username, String email, String rawPw, Role role) {
        return User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPw))
                .role(role)
                .active(true)
                .build();
    }

    // ── Categories ───────────────────────────────────────────────────────────

    private List<Category> seedCategories() {
        List<Category> cats = categoryRepository.saveAll(List.of(
            cat("Electronics",      "Gadgets, phones, accessories"),
            cat("Food & Snacks",    "Packaged food, snacks and confectionery"),
            cat("Beverages",        "Water, juices, soft drinks and energy drinks"),
            cat("Clothing",         "T-shirts, jeans, jackets and accessories"),
            cat("Home & Kitchen",   "Appliances, cookware and home essentials"),
            cat("Beauty & Care",    "Skincare, haircare and personal hygiene")
        ));
        log.info("Seeded {} categories", cats.size());
        return cats;
    }

    private Category cat(String name, String desc) {
        return Category.builder().name(name).description(desc).build();
    }

    // ── Products & Inventory ─────────────────────────────────────────────────

    private void seedProductsAndInventory(List<Category> cats) {
        Category electronics = cats.get(0);
        Category food        = cats.get(1);
        Category beverages   = cats.get(2);
        Category clothing    = cats.get(3);
        Category home        = cats.get(4);
        Category beauty      = cats.get(5);

        List<Product> products = productRepository.saveAll(List.of(

            // Electronics
            product("Wireless Earbuds Pro",    "SKU-E001", "4901234560011", new BigDecimal("49.99"),  electronics),
            product("USB-C Fast Charger 65W",  "SKU-E002", "4901234560028", new BigDecimal("19.99"),  electronics),
            product("Bluetooth Speaker Mini",  "SKU-E003", "4901234560035", new BigDecimal("34.99"),  electronics),
            product("Smartphone Screen Guard", "SKU-E004", "4901234560042", new BigDecimal("8.99"),   electronics),
            product("Portable Power Bank 10K", "SKU-E005", "4901234560059", new BigDecimal("29.99"),  electronics),

            // Food & Snacks
            product("Salted Potato Chips 200g","SKU-F001", "4901234561018", new BigDecimal("2.49"),   food),
            product("Dark Chocolate Bar 100g", "SKU-F002", "4901234561025", new BigDecimal("3.99"),   food),
            product("Mixed Nuts 250g",         "SKU-F003", "4901234561032", new BigDecimal("6.99"),   food),
            product("Instant Noodles Pack",    "SKU-F004", "4901234561049", new BigDecimal("1.49"),   food),
            product("Organic Granola Bar",     "SKU-F005", "4901234561056", new BigDecimal("2.99"),   food),

            // Beverages
            product("Mineral Water 500ml",     "SKU-B001", "4901234562015", new BigDecimal("0.99"),   beverages),
            product("Orange Juice 1L",         "SKU-B002", "4901234562022", new BigDecimal("3.49"),   beverages),
            product("Energy Drink 250ml",      "SKU-B003", "4901234562039", new BigDecimal("2.29"),   beverages),
            product("Green Tea 12-Pack",       "SKU-B004", "4901234562046", new BigDecimal("5.99"),   beverages),
            product("Cold Brew Coffee 330ml",  "SKU-B005", "4901234562053", new BigDecimal("4.49"),   beverages),

            // Clothing
            product("Classic White T-Shirt M", "SKU-C001", "4901234563012", new BigDecimal("14.99"),  clothing),
            product("Slim Fit Jeans 32x30",    "SKU-C002", "4901234563029", new BigDecimal("39.99"),  clothing),
            product("Sport Socks 3-Pack",      "SKU-C003", "4901234563036", new BigDecimal("9.99"),   clothing),
            product("Baseball Cap — Black",    "SKU-C004", "4901234563043", new BigDecimal("12.99"),  clothing),

            // Home & Kitchen
            product("Stainless Steel Mug 350ml","SKU-H001","4901234564019", new BigDecimal("11.99"),  home),
            product("Non-Stick Pan 24cm",      "SKU-H002", "4901234564026", new BigDecimal("22.99"),  home),
            product("Dish Soap 500ml",         "SKU-H003", "4901234564033", new BigDecimal("3.29"),   home),

            // Beauty & Care
            product("SPF 50 Sunscreen 100ml",  "SKU-P001", "4901234565016", new BigDecimal("8.99"),   beauty),
            product("Moisturising Shampoo 400ml","SKU-P002","4901234565023", new BigDecimal("7.49"),  beauty),
            product("Hand Sanitiser 250ml",    "SKU-P003", "4901234565030", new BigDecimal("4.99"),   beauty)
        ));

        // Seed inventory for each product with realistic quantities
        int[] qtys       = {45, 80, 30, 120, 25, 200, 150, 90, 300, 180, 500, 100, 75, 60, 40, 35, 20, 60, 45, 70, 15, 250, 80, 110, 160};
        int[] thresholds = {10, 15, 10, 20,  5,  30, 25,  15, 50,  30,  50, 20,  10, 10, 10, 10, 5,  10, 10, 15, 5,  30, 15,  20,  25};

        for (int i = 0; i < products.size(); i++) {
            inventoryRepository.save(
                Inventory.builder()
                    .product(products.get(i))
                    .quantity(qtys[i])
                    .lowStockThreshold(thresholds[i])
                    .build()
            );
        }

        log.info("Seeded {} products with inventory", products.size());
    }

    private Product product(String name, String sku, String barcode, BigDecimal price, Category cat) {
        return Product.builder()
                .name(name)
                .sku(sku)
                .barcode(barcode)
                .price(price)
                .category(cat)
                .active(true)
                .build();
    }
}
