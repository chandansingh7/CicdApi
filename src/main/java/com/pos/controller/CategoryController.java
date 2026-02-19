package com.pos.controller;

import com.pos.dto.request.CategoryRequest;
import com.pos.dto.response.ApiResponse;
import com.pos.dto.response.CategoryResponse;
import com.pos.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Category created", categoryService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable Long id,
                                                                @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Category updated", categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Category deleted", null));
    }
}
