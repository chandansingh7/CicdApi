package com.pos.service;

import com.pos.dto.request.LabelRequest;
import com.pos.dto.response.LabelResponse;
import com.pos.dto.response.ProductResponse;
import com.pos.entity.Category;
import com.pos.entity.Inventory;
import com.pos.entity.Label;
import com.pos.entity.Product;
import com.pos.exception.BadRequestException;
import com.pos.exception.ErrorCode;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CategoryRepository;
import com.pos.repository.InventoryRepository;
import com.pos.repository.LabelRepository;
import com.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public Page<LabelResponse> getAll(String search, Long categoryId, Pageable pageable) {
        log.debug("Fetching labels — search: '{}', categoryId: {}", search, categoryId);
        if (search != null && !search.isBlank()) {
            if (categoryId != null) {
                return labelRepository.searchUnlinkedByCategory(search, categoryId, pageable)
                        .map(LabelResponse::from);
            }
            return labelRepository.searchUnlinked(search, pageable).map(LabelResponse::from);
        }
        if (categoryId != null) {
            return labelRepository.findByCategoryIdAndUnlinked(categoryId, pageable)
                    .map(LabelResponse::from);
        }
        return labelRepository.findAllUnlinked(pageable).map(LabelResponse::from);
    }

    public LabelResponse getById(Long id) {
        log.debug("Fetching label id: {}", id);
        return LabelResponse.from(findById(id));
    }

    @Transactional
    public LabelResponse create(LabelRequest request) {
        log.info("Creating label — barcode: '{}', name: '{}'", request.getBarcode(), request.getName());
        if (labelRepository.existsByBarcode(request.getBarcode().trim())) {
            throw new BadRequestException(ErrorCode.LB002);
        }
        if (productRepository.existsByBarcode(request.getBarcode().trim())) {
            throw new BadRequestException(ErrorCode.LB002);
        }

        Category category = resolveCategory(request.getCategoryId());

        Label label = Label.builder()
                .barcode(request.getBarcode().trim())
                .name(request.getName().trim())
                .price(request.getPrice())
                .sku(request.getSku() != null ? request.getSku().trim() : null)
                .category(category)
                .build();
        label = labelRepository.save(label);

        log.info("Label created — id: {}, barcode: '{}'", label.getId(), label.getBarcode());
        return LabelResponse.from(label);
    }

    @Transactional
    public LabelResponse update(Long id, LabelRequest request) {
        log.info("Updating label id: {}", id);
        Label label = findById(id);
        if (label.getProduct() != null) {
            throw new BadRequestException(ErrorCode.LB001, "Cannot edit a label that is linked to a product");
        }

        String newBarcode = request.getBarcode().trim();
        if (!newBarcode.equals(label.getBarcode())) {
            if (labelRepository.existsByBarcode(newBarcode) || productRepository.existsByBarcode(newBarcode)) {
                throw new BadRequestException(ErrorCode.LB002);
            }
        }

        Category category = resolveCategory(request.getCategoryId());
        label.setBarcode(newBarcode);
        label.setName(request.getName().trim());
        label.setPrice(request.getPrice());
        label.setSku(request.getSku() != null ? request.getSku().trim() : null);
        label.setCategory(category);

        log.info("Label updated — id: {}", id);
        return LabelResponse.from(labelRepository.save(label));
    }

    @Transactional
    public ProductResponse addAsProduct(Long labelId, int initialStock) {
        log.info("Converting label {} to product", labelId);
        Label label = findById(labelId);
        if (label.getProduct() != null) {
            throw new BadRequestException(ErrorCode.LB001, "Label is already linked to a product");
        }
        if (productRepository.existsByBarcode(label.getBarcode())) {
            throw new BadRequestException(ErrorCode.LB002);
        }
        if (label.getSku() != null && !label.getSku().isBlank() && productRepository.existsBySku(label.getSku())) {
            throw new BadRequestException(ErrorCode.PR002);
        }

        Product product = Product.builder()
                .name(label.getName())
                .sku(label.getSku())
                .barcode(label.getBarcode())
                .price(label.getPrice())
                .category(label.getCategory())
                .active(true)
                .updatedBy(currentUsername())
                .build();
        product = productRepository.save(product);

        inventoryRepository.save(Inventory.builder()
                .product(product)
                .quantity(initialStock)
                .lowStockThreshold(10)
                .build());

        label.setProduct(product);
        labelRepository.save(label);

        log.info("Label {} converted to product id: {}", labelId, product.getId());
        return toProductResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting label id: {}", id);
        Label label = findById(id);
        if (label.getProduct() != null) {
            throw new BadRequestException(ErrorCode.LB001, "Cannot delete a label linked to a product");
        }
        labelRepository.delete(label);
    }

    private Label findById(Long id) {
        return labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.LB001, "id: " + id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CT001, "id: " + categoryId));
    }

    private String currentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    private ProductResponse toProductResponse(Product p) {
        int qty = inventoryRepository.findByProductId(p.getId())
                .map(Inventory::getQuantity)
                .orElse(0);
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .sku(p.getSku())
                .barcode(p.getBarcode())
                .price(p.getPrice())
                .categoryId(p.getCategory() != null ? p.getCategory().getId() : null)
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : null)
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .quantity(qty)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .updatedBy(p.getUpdatedBy())
                .build();
    }
}
