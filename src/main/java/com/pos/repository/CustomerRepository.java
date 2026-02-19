package com.pos.repository;

import com.pos.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "c.phone LIKE CONCAT('%', :query, '%')")
    Page<Customer> search(@Param("query") String query, Pageable pageable);
}
