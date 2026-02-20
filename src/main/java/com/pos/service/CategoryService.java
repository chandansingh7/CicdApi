package com.pos.service;

import com.pos.dto.request.CategoryRequest;
import com.pos.dto.response.CategoryResponse;
import com.pos.entity.Category;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CategoryRepository;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {
        log.debug("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    public CategoryResponse getById(Long id) {
        log.debug("Fetching category id: {}", id);
        return CategoryResponse.from(findById(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        log.info("Creating category: '{}'", request.getName());
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Category creation failed — name already exists: '{}'", request.getName());
            throw new BadRequestException("Category already exists: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .updatedBy(currentUsername())
                .build();
        CategoryResponse saved = CategoryResponse.from(categoryRepository.save(category));
        log.info("Category created — id: {}, name: '{}'", saved.getId(), saved.getName());
        return saved;
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        log.info("Updating category id: {}", id);
        Category category = findById(id);
        categoryRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                log.warn("Category update failed — name already in use: '{}'", request.getName());
                throw new BadRequestException("Category name already in use: " + request.getName());
            }
        });
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setUpdatedBy(currentUsername());
        CategoryResponse saved = CategoryResponse.from(categoryRepository.save(category));
        log.info("Category updated — id: {}, name: '{}'", id, request.getName());
        return saved;
    }

    public void delete(Long id) {
        log.info("Deleting category id: {}", id);
        Category category = findById(id);
        categoryRepository.delete(category);
        log.info("Category id: {} deleted", id);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
