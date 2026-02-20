package com.pos.service;

import com.pos.dto.request.OrderItemRequest;
import com.pos.dto.request.OrderRequest;
import com.pos.dto.response.OrderResponse;
import com.pos.entity.*;
import com.pos.enums.OrderStatus;
import com.pos.enums.PaymentStatus;
import com.pos.exception.BadRequestException;
import com.pos.exception.ResourceNotFoundException;
import com.pos.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    private final OrderRepository     orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository   productRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository  customerRepository;
    private final UserRepository      userRepository;
    private final PaymentRepository   paymentRepository;

    public Page<OrderResponse> getAll(Pageable pageable) {
        log.debug("Fetching orders — page: {}", pageable.getPageNumber());
        return orderRepository.findAll(pageable).map(OrderResponse::from);
    }

    public OrderResponse getById(Long id) {
        log.debug("Fetching order id: {}", id);
        return OrderResponse.from(findById(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Creating order — cashier: '{}', customerId: {}, items: {}",
                username, request.getCustomerId(),
                request.getItems() != null ? request.getItems().size() : 0);

        User cashier = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
        }

        List<OrderItem> items    = new ArrayList<>();
        BigDecimal      subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

            if (!product.isActive()) {
                log.warn("Order rejected — product not available: '{}'", product.getName());
                throw new BadRequestException("Product is not available: " + product.getName());
            }

            Inventory inventory = inventoryRepository.findByProductId(product.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for: " + product.getName()));

            if (inventory.getQuantity() < itemReq.getQuantity()) {
                log.warn("Order rejected — insufficient stock for '{}': available {}, requested {}",
                        product.getName(), inventory.getQuantity(), itemReq.getQuantity());
                throw new BadRequestException(
                        "Insufficient stock for " + product.getName() +
                        ". Available: " + inventory.getQuantity());
            }

            BigDecimal itemSubtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            items.add(OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(itemSubtotal)
                    .build());

            subtotal = subtotal.add(itemSubtotal);
            inventory.setQuantity(inventory.getQuantity() - itemReq.getQuantity());
            inventoryRepository.save(inventory);
            log.debug("Reserved {} units of product id: {}", itemReq.getQuantity(), product.getId());
        }

        BigDecimal discount     = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal afterDiscount = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal tax          = afterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total        = afterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP);

        Order order = Order.builder()
                .customer(customer)
                .cashier(cashier)
                .subtotal(subtotal)
                .tax(tax)
                .discount(discount)
                .total(total)
                .status(OrderStatus.COMPLETED)
                .paymentMethod(request.getPaymentMethod())
                .build();

        order = orderRepository.save(order);

        for (OrderItem item : items) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(items);
        order.setItems(items);

        paymentRepository.save(Payment.builder()
                .order(order)
                .method(request.getPaymentMethod())
                .amount(total)
                .status(PaymentStatus.COMPLETED)
                .build());

        log.info("Order created — id: {}, total: {}, payment: {}, cashier: '{}'",
                order.getId(), total, request.getPaymentMethod(), username);
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        log.info("Cancelling order id: {}", id);
        Order order = findById(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Cancel rejected — order id: {} is already cancelled", id);
            throw new BadRequestException("Order is already cancelled");
        }
        if (order.getStatus() == OrderStatus.REFUNDED) {
            log.warn("Cancel rejected — order id: {} is already refunded", id);
            throw new BadRequestException("Cannot cancel a refunded order");
        }

        for (OrderItem item : order.getItems()) {
            inventoryRepository.findByProductId(item.getProduct().getId()).ifPresent(inv -> {
                inv.setQuantity(inv.getQuantity() + item.getQuantity());
                inventoryRepository.save(inv);
                log.debug("Restored {} units of product id: {}", item.getQuantity(), item.getProduct().getId());
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        paymentRepository.findByOrderId(id).ifPresent(p -> {
            p.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(p);
        });

        OrderResponse saved = OrderResponse.from(orderRepository.save(order));
        log.info("Order id: {} cancelled and inventory restored", id);
        return saved;
    }

    private Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
