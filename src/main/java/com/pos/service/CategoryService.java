package com.pos.service;

import com.pos.dto.request.CategoryRequest;
import com.pos.dto.response.CategoryResponse;
import com.pos.entity.Category;
import com.pos.exception.BadRequestException;
import com.pos.exception.ErrorCode;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Page<CategoryResponse> getAll(Pageable pageable) {
        log.debug("Fetching categories page: {}", pageable);
        return categoryRepository.findAll(pageable).map(CategoryResponse::from);
    }

    /** For dropdowns and small-list use; returns up to 500 categories. */
    public List<CategoryResponse> getList() {
        log.debug("Fetching category list for dropdown");
        return categoryRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 500))
                .map(CategoryResponse::from).getContent();
    }

    public CategoryResponse getById(Long id) {
        log.debug("Fetching category id: {}", id);
        return CategoryResponse.from(findById(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        log.info("Creating category: '{}'", request.getName());
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("[CT002] Category name already exists: '{}'", request.getName());
            throw new BadRequestException(ErrorCode.CT002);
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
                log.warn("[CT002] Category name in use: '{}'", request.getName());
                throw new BadRequestException(ErrorCode.CT002);
            }
        });
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setUpdatedBy(currentUsername());
        log.info("Category updated — id: {}", id);
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        log.info("Deleting category id: {}", id);
        categoryRepository.delete(findById(id));
        log.info("Category id: {} deleted", id);
    }

    public com.pos.dto.response.CountStats getStats() {
        log.debug("Fetching category stats");
        return new com.pos.dto.response.CountStats(categoryRepository.count());
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CT001));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
