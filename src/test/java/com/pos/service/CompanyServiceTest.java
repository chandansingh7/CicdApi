package com.pos.service;

import com.pos.dto.request.CompanyRequest;
import com.pos.dto.response.CompanyResponse;
import com.pos.entity.Company;
import com.pos.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock private CompanyRepository companyRepository;
    @Mock private ImageStorageService imageStorageService;

    @InjectMocks
    private CompanyService companyService;

    private Company existingCompany;

    @BeforeEach
    void setUp() {
        existingCompany = new Company();
        existingCompany.setId(1L);
        existingCompany.setName("My Store");
        existingCompany.setAddress("123 Main St");
        existingCompany.setReceiptPaperSize("80mm");
    }

    @Test
    void get_whenNoCompany_returnsNullResponse() {
        when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        CompanyResponse response = companyService.get();
        assertThat(response).isNull();
    }

    @Test
    void get_whenCompanyExists_returnsMappedResponse() {
        when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingCompany));
        CompanyResponse response = companyService.get();
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("My Store");
        assertThat(response.getAddress()).isEqualTo("123 Main St");
    }

    @Test
    void update_whenNoCompany_createsNewAndSaves() {
        when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        CompanyRequest request = new CompanyRequest();
        request.setName("New Store");
        request.setReceiptPaperSize("A4");

        CompanyResponse response = companyService.update(request, "admin");

        assertThat(response).isNotNull();
        verify(companyRepository, org.mockito.Mockito.times(2)).save(any(Company.class));
    }

    @Test
    void update_whenCompanyExists_updatesAndSaves() {
        when(companyRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(existingCompany));
        when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

        CompanyRequest request = new CompanyRequest();
        request.setName("Updated Name");
        request.setAddress("456 Oak Ave");
        request.setReceiptPaperSize("58mm");

        CompanyResponse response = companyService.update(request, "admin");

        assertThat(response).isNotNull();
        assertThat(existingCompany.getName()).isEqualTo("Updated Name");
        assertThat(existingCompany.getAddress()).isEqualTo("456 Oak Ave");
        assertThat(existingCompany.getReceiptPaperSize()).isEqualTo("58mm");
        assertThat(existingCompany.getUpdatedBy()).isEqualTo("admin");
        verify(companyRepository).save(existingCompany);
    }
}
