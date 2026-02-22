package com.pos.controller;

import com.pos.dto.request.CompanyRequest;
import com.pos.dto.response.ApiResponse;
import com.pos.dto.response.CompanyResponse;
import com.pos.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> get() {
        return ResponseEntity.ok(ApiResponse.ok(companyService.get()));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<CompanyResponse>> update(
            @Valid @RequestBody CompanyRequest request,
            Authentication auth) {
        String username = auth != null ? auth.getName() : "system";
        return ResponseEntity.ok(ApiResponse.ok("Company updated", companyService.update(request, username)));
    }
}
