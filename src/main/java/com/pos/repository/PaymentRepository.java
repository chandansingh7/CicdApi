package com.pos.repository;

import com.pos.entity.Payment;
import com.pos.entity.User;
import com.pos.enums.PaymentMethod;
import com.pos.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    @Query("select coalesce(sum(p.amount), 0) from Payment p " +
           "where p.method = :method and p.status = :status " +
           "and p.order.cashier = :cashier " +
           "and p.createdAt between :from and :to")
    BigDecimal sumByMethodAndStatusAndCashierAndCreatedAtBetween(
            @Param("method") PaymentMethod method,
            @Param("status") PaymentStatus status,
            @Param("cashier") User cashier,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}

