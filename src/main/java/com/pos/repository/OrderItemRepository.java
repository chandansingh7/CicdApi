package com.pos.repository;

import com.pos.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) AS totalQty " +
           "FROM OrderItem oi WHERE oi.order.status = 'COMPLETED' " +
           "AND oi.order.createdAt BETWEEN :from AND :to " +
           "GROUP BY oi.product.id, oi.product.name ORDER BY totalQty DESC")
    List<Object[]> findTopProductsBetween(@Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to,
                                          org.springframework.data.domain.Pageable pageable);
}
