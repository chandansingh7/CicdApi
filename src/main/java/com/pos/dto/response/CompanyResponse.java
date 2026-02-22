package com.pos.dto.response;

import com.pos.entity.Company;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyResponse {

    private Long id;
    private String name;
    private String logoUrl;
    private String address;
    private String phone;
    private String email;
    private String taxId;
    private String website;
    private String receiptFooterText;
    private String receiptPaperSize;
    private String receiptHeaderText;

    public static CompanyResponse from(Company c) {
        if (c == null) return null;
        return CompanyResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .logoUrl(c.getLogoUrl())
                .address(c.getAddress())
                .phone(c.getPhone())
                .email(c.getEmail())
                .taxId(c.getTaxId())
                .website(c.getWebsite())
                .receiptFooterText(c.getReceiptFooterText())
                .receiptPaperSize(c.getReceiptPaperSize())
                .receiptHeaderText(c.getReceiptHeaderText())
                .build();
    }
}
