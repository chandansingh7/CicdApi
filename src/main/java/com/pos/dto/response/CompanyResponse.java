package com.pos.dto.response;

import com.pos.entity.Company;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompanyResponse {

    private Long id;
    private String name;
    private String logoUrl;
    private String faviconUrl;
    private String address;
    private String phone;
    private String email;
    private String taxId;
    private String website;
    private String receiptFooterText;
    private String receiptPaperSize;
    private String receiptHeaderText;
    private LocalDateTime updatedAt;

    public static CompanyResponse from(Company c) {
        if (c == null) return null;
        return CompanyResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .logoUrl(c.getLogoUrl())
                .faviconUrl(c.getFaviconUrl())
                .address(c.getAddress())
                .phone(c.getPhone())
                .email(c.getEmail())
                .taxId(c.getTaxId())
                .website(c.getWebsite())
                .receiptFooterText(c.getReceiptFooterText())
                .receiptPaperSize(c.getReceiptPaperSize())
                .receiptHeaderText(c.getReceiptHeaderText())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
