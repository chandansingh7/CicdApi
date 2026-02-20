package com.pos.service;

import com.pos.dto.request.CustomerRequest;
import com.pos.dto.response.CustomerResponse;
import com.pos.entity.Customer;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Page<CustomerResponse> getAll(String search, Pageable pageable) {
        log.debug("Fetching customers — search: '{}', page: {}", search, pageable.getPageNumber());
        if (search != null && !search.isBlank()) {
            return customerRepository.search(search, pageable).map(CustomerResponse::from);
        }
        return customerRepository.findAll(pageable).map(CustomerResponse::from);
    }

    public CustomerResponse getById(Long id) {
        log.debug("Fetching customer id: {}", id);
        return CustomerResponse.from(findById(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        log.info("Creating customer — name: '{}', email: '{}'", request.getName(), request.getEmail());
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            log.warn("Customer creation failed — email already registered: {}", request.getEmail());
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .updatedBy(currentUsername())
                .build();
        CustomerResponse saved = CustomerResponse.from(customerRepository.save(customer));
        log.info("Customer created — id: {}, name: '{}'", saved.getId(), saved.getName());
        return saved;
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        log.info("Updating customer id: {}", id);
        Customer customer = findById(id);
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            customerRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    log.warn("Customer update failed — email already in use: {}", request.getEmail());
                    throw new BadRequestException("Email already in use: " + request.getEmail());
                }
            });
        }
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setUpdatedBy(currentUsername());
        CustomerResponse saved = CustomerResponse.from(customerRepository.save(customer));
        log.info("Customer updated — id: {}, name: '{}'", id, request.getName());
        return saved;
    }

    public void delete(Long id) {
        log.info("Deleting customer id: {}", id);
        Customer customer = findById(id);
        customerRepository.delete(customer);
        log.info("Customer id: {} deleted", id);
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
