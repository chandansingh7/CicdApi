package com.pos.service;

import com.pos.dto.request.CategoryRequest;
import com.pos.dto.response.CategoryResponse;
import com.pos.entity.Category;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of()));
        category = Category.builder().id(1L).name("Electronics").description("Gadgets").build();
    }

    @Test
    void getAll_returnsMappedList() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        List<CategoryResponse> result = categoryService.getAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getById_existing_returnsResponse() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        CategoryResponse response = categoryService.getById(1L);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Electronics");
    }

    @Test
    void getById_notFound_throws() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> categoryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_duplicateName_throwsBadRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Electronics");
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);
        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void create_valid_savesAndReturns() {
        CategoryRequest request = new CategoryRequest();
        request.setName("New Cat");
        request.setDescription("Desc");
        when(categoryRepository.existsByName("New Cat")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });
        CategoryResponse response = categoryService.create(request);
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("New Cat");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void update_existing_savesAndReturns() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName("Updated")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated");
        request.setDescription("New desc");
        CategoryResponse response = categoryService.update(1L, request);
        assertThat(response).isNotNull();
        assertThat(category.getName()).isEqualTo("Updated");
        assertThat(category.getDescription()).isEqualTo("New desc");
    }

    @Test
    void delete_existing_deletes() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        categoryService.delete(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void getStats_returnsCount() {
        when(categoryRepository.count()).thenReturn(5L);
        var stats = categoryService.getStats();
        assertThat(stats).isNotNull();
        assertThat(stats.total()).isEqualTo(5L);
    }
}
