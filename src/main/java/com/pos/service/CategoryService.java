package com.pos.service;

import com.pos.dto.request.CategoryRequest;
import com.pos.dto.response.CategoryResponse;
import com.pos.entity.Category;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    public CategoryResponse getById(Long id) {
        return CategoryResponse.from(findById(id));
    }

    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category already exists: " + request.getName());
        }
        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = findById(id);
        categoryRepository.findByName(request.getName()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Category name already in use: " + request.getName());
            }
        });
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return CategoryResponse.from(categoryRepository.save(category));
    }

    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
