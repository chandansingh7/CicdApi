package com.pos.service;

import com.pos.dto.request.InventoryUpdateRequest;
import com.pos.dto.response.InventoryResponse;
import com.pos.entity.Inventory;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.InventoryRepository;
import com.pos.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository   productRepository;

    public List<InventoryResponse> getAll() {
        log.debug("Fetching all inventory records");
        return inventoryRepository.findAll().stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
    }

    public List<InventoryResponse> getLowStock() {
        log.debug("Fetching low-stock inventory items");
        List<InventoryResponse> items = inventoryRepository.findLowStockItems().stream()
                .map(InventoryResponse::from)
                .collect(Collectors.toList());
        if (!items.isEmpty()) {
            log.info("Low-stock alert: {} product(s) below threshold", items.size());
        }
        return items;
    }

    public InventoryResponse getByProductId(Long productId) {
        log.debug("Fetching inventory for product id: {}", productId);
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product: " + productId));
        return InventoryResponse.from(inventory);
    }

    public InventoryResponse updateStock(Long productId, InventoryUpdateRequest request) {
        log.info("Updating stock for product id: {} — qty: {}, lowStockThreshold: {}",
                productId, request.getQuantity(), request.getLowStockThreshold());
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", productId);
        }
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product: " + productId));

        int oldQty = inventory.getQuantity();
        inventory.setQuantity(request.getQuantity());
        inventory.setLowStockThreshold(request.getLowStockThreshold());
        inventory.setUpdatedBy(currentUsername());
        InventoryResponse saved = InventoryResponse.from(inventoryRepository.save(inventory));
        log.info("Stock updated for product id: {} — {} → {}",
                productId, oldQty, request.getQuantity());
        return saved;
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
