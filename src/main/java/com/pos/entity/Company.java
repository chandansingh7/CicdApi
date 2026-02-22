package com.pos.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Single-row company profile: name, contact, and receipt/bill settings.
 * Used for logo display, bills, and printer options.
 */
@Entity
@Table(name = "company")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String logoUrl;

    private String address;

    private String phone;

    private String email;

    private String taxId;

    private String website;

    /** Optional footer line on receipts (e.g. "Thank you!") */
    private String receiptFooterText;

    /** Receipt paper size: 58mm, 80mm, A4 */
    @Column(name = "receipt_paper_size", length = 20)
    private String receiptPaperSize;

    /** Optional custom header line on receipts */
    private String receiptHeaderText;

    private LocalDateTime updatedAt;

    private String updatedBy;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
