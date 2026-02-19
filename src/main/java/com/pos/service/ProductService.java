package com.pos.service;

import com.pos.dto.request.ProductRequest;
import com.pos.dto.response.ProductResponse;
import com.pos.entity.Category;
import com.pos.entity.Inventory;
import com.pos.entity.Product;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CategoryRepository;
import com.pos.repository.InventoryRepository;
import com.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;

    public Page<ProductResponse> getAll(String search, Long categoryId, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return productRepository.searchActive(search, pageable)
                    .map(p -> toResponse(p));
        }
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                    .map(p -> toResponse(p));
        }
        return productRepository.findByActiveTrue(pageable).map(p -> toResponse(p));
    }

    public ProductResponse getById(Long id) {
        return toResponse(findById(id));
    }

    public ProductResponse getByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }
        if (request.getBarcode() != null && productRepository.existsByBarcode(request.getBarcode())) {
            throw new BadRequestException("Barcode already exists: " + request.getBarcode());
        }

        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()))
                : null;

        Product product = Product.builder()
                .name(request.getName())
                .sku(request.getSku())
                .barcode(request.getBarcode())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .active(request.isActive())
                .build();

        product = productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(request.getInitialStock())
                .lowStockThreshold(request.getLowStockThreshold())
                .build();
        inventoryRepository.save(inventory);

        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findById(id);

        if (request.getSku() != null && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }

        Category category = request.getCategoryId() != null
                ? categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()))
                : null;

        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setBarcode(request.getBarcode());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.isActive());

        return toResponse(productRepository.save(product));
    }

    public void delete(Long id) {
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private ProductResponse toResponse(Product product) {
        int qty = inventoryRepository.findByProductId(product.getId())
                .map(Inventory::getQuantity).orElse(0);
        return ProductResponse.from(product, qty);
    }
}
