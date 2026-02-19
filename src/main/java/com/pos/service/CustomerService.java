package com.pos.service;

import com.pos.dto.request.CustomerRequest;
import com.pos.dto.response.CustomerResponse;
import com.pos.entity.Customer;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Page<CustomerResponse> getAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return customerRepository.search(search, pageable).map(CustomerResponse::from);
        }
        return customerRepository.findAll(pageable).map(CustomerResponse::from);
    }

    public CustomerResponse getById(Long id) {
        return CustomerResponse.from(findById(id));
    }

    public CustomerResponse create(CustomerRequest request) {
        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }
        Customer customer = Customer.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            customerRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException("Email already in use: " + request.getEmail());
                }
            });
        }
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public void delete(Long id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
    }

    private Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }
}
