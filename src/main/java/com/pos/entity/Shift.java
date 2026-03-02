package com.pos.entity;

import com.pos.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cashier_id", nullable = false)
    private User cashier;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal openingFloat;

    @Column(precision = 10, scale = 2)
    private BigDecimal cashSales;

    @Column(precision = 10, scale = 2)
    private BigDecimal expectedCash;

    @Column(precision = 10, scale = 2)
    private BigDecimal countedCash;

    @Column(precision = 10, scale = 2)
    private BigDecimal difference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        if (openedAt == null) {
            openedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ShiftStatus.OPEN;
        }
    }
}

