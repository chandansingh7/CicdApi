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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository    productRepository;
    private final CategoryRepository   categoryRepository;
    private final InventoryRepository  inventoryRepository;
    private final ImageStorageService  imageStorageService;

    public Page<ProductResponse> getAll(String search, Long categoryId, Pageable pageable) {
        log.debug("Fetching products — search: '{}', categoryId: {}, page: {}",
                search, categoryId, pageable.getPageNumber());

        if (search != null && !search.isBlank()) {
            return productRepository.searchActive(search, pageable).map(this::toResponse);
        }
        if (categoryId != null) {
            return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable).map(this::toResponse);
        }
        return productRepository.findByActiveTrue(pageable).map(this::toResponse);
    }

    public ProductResponse getById(Long id) {
        log.debug("Fetching product id: {}", id);
        return toResponse(findById(id));
    }

    public ProductResponse getByBarcode(String barcode) {
        log.debug("Fetching product by barcode: {}", barcode);
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with barcode: " + barcode));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Creating product — name: '{}', sku: '{}'", request.getName(), request.getSku());

        if (request.getSku() != null && productRepository.existsBySku(request.getSku())) {
            log.warn("Product creation failed — SKU already exists: {}", request.getSku());
            throw new BadRequestException("SKU already exists: " + request.getSku());
        }
        if (request.getBarcode() != null && productRepository.existsByBarcode(request.getBarcode())) {
            log.warn("Product creation failed — barcode already exists: {}", request.getBarcode());
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
                .updatedBy(currentUsername())
                .build();

        product = productRepository.save(product);

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantity(request.getInitialStock())
                .lowStockThreshold(request.getLowStockThreshold())
                .build();
        inventoryRepository.save(inventory);

        log.info("Product created — id: {}, name: '{}', sku: '{}'",
                product.getId(), product.getName(), product.getSku());
        return toResponse(product);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Updating product id: {}", id);
        Product product = findById(id);

        if (request.getSku() != null && !request.getSku().equals(product.getSku())
                && productRepository.existsBySku(request.getSku())) {
            log.warn("Product update failed — SKU already exists: {}", request.getSku());
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
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            product.setImageUrl(request.getImageUrl());
        }
        product.setActive(request.isActive());
        product.setUpdatedBy(currentUsername());

        ProductResponse saved = toResponse(productRepository.save(product));
        log.info("Product updated — id: {}, name: '{}'", id, request.getName());
        return saved;
    }

    public void delete(Long id) {
        log.info("Soft-deleting product id: {}", id);
        Product product = findById(id);
        product.setActive(false);
        productRepository.save(product);
        log.info("Product id: {} marked inactive", id);
    }

    @Transactional
    public ProductResponse uploadImage(Long id, MultipartFile file) {
        log.info("Uploading image for product id: {}, size: {} bytes", id,
                file != null ? file.getSize() : 0);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("File must be an image (JPEG, PNG, GIF or WebP)");
        }
        Product product = findById(id);
        try {
            String imageUrl = imageStorageService.store(id, file);
            product.setImageUrl(imageUrl);
            product.setUpdatedBy(currentUsername());
            ProductResponse saved = toResponse(productRepository.save(product));
            log.info("Image uploaded for product id: {} — url: {}", id, imageUrl);
            return saved;
        } catch (IOException ex) {
            log.error("Failed to store image for product id: {} — {}", id, ex.getMessage(), ex);
            throw new RuntimeException("Failed to store image: " + ex.getMessage(), ex);
        }
    }

    private Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private ProductResponse toResponse(Product product) {
        int qty = inventoryRepository.findByProductId(product.getId())
                .map(Inventory::getQuantity).orElse(0);
        return ProductResponse.from(product, qty);
    }
}
