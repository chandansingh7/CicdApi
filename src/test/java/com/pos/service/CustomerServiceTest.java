package com.pos.service;

import com.pos.dto.request.CustomerRequest;
import com.pos.dto.response.CustomerResponse;
import com.pos.entity.Customer;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null, List.of()));
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .phone("555-1234")
                .build();
    }

    @Test
    void getAll_noSearch_returnsPage() {
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(customer)));
        var result = customerService.getAll(null, org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John Doe");
    }

    @Test
    void getById_existing_returnsResponse() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        CustomerResponse response = customerService.getById(1L);
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getById_notFound_throws() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> customerService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_duplicateEmail_throwsBadRequest() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Jane");
        request.setEmail("john@example.com");
        when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);
        assertThatThrownBy(() -> customerService.create(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void create_valid_savesAndReturns() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Jane");
        request.setEmail("jane@example.com");
        request.setPhone("555-5678");
        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> {
            Customer c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });
        CustomerResponse response = customerService.create(request);
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jane");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void update_existing_savesAndReturns() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("updated@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));
        CustomerRequest request = new CustomerRequest();
        request.setName("John Updated");
        request.setEmail("updated@example.com");
        request.setPhone("555-9999");
        CustomerResponse response = customerService.update(1L, request);
        assertThat(response).isNotNull();
        assertThat(customer.getName()).isEqualTo("John Updated");
        assertThat(customer.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void delete_existing_deletes() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        customerService.delete(1L);
        verify(customerRepository).delete(customer);
    }

    @Test
    void getStats_returnsCount() {
        when(customerRepository.count()).thenReturn(10L);
        var stats = customerService.getStats();
        assertThat(stats).isNotNull();
        assertThat(stats.total()).isEqualTo(10L);
    }
}
