package com.pos.controller;

import com.pos.dto.request.LabelRequest;
import com.pos.dto.response.ApiResponse;
import com.pos.dto.response.LabelResponse;
import com.pos.dto.response.ProductResponse;
import com.pos.service.LabelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final LabelService labelService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<LabelResponse>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(labelService.getAll(search, categoryId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LabelResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(labelService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LabelResponse>> create(@Valid @RequestBody LabelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Label created", labelService.create(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<java.util.List<LabelResponse>>> createBulk(
            @Valid @RequestBody java.util.List<LabelRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Labels created", labelService.createBulk(requests)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LabelResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LabelRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Label updated", labelService.update(id, request)));
    }

    @PostMapping("/{id}/add-as-product")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ProductResponse>> addAsProduct(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int initialStock) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Product created from label", labelService.addAsProduct(id, initialStock)));
    }

    @PostMapping("/{id}/attach")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LabelResponse>> attachToProduct(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(
                ApiResponse.ok("Label attached to product", labelService.attachToProduct(id, productId, force)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        labelService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Label deleted", null));
    }
}
