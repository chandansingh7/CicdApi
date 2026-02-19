package com.pos.repository;

import com.pos.entity.Order;
import com.pos.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCashierId(Long cashierId, Pageable pageable);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    List<Order> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    long countCompletedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt BETWEEN :from AND :to")
    BigDecimal sumTotalBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
