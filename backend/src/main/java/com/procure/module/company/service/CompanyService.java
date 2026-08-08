package com.procure.module.company.service;

import com.procure.common.exception.BusinessException;
import com.procure.module.company.dto.CompanyDtos.CompanyRequest;
import com.procure.module.company.dto.CompanyDtos.CompanyResponse;
import com.procure.module.company.entity.Company;
import com.procure.module.company.repository.CompanyRepository;
import com.procure.module.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AuditLogService auditLogService;

    public CompanyResponse createCompany(CompanyRequest request) {
        if (companyRepository.existsByCode(request.getCode())) {
            throw BusinessException.conflict("Company code already exists: " + request.getCode());
        }
        if (request.getRegistrationNumber() != null && !request.getRegistrationNumber().isBlank()
                && companyRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw BusinessException.conflict("Registration number already registered: " + request.getRegistrationNumber());
        }

        Company company = Company.builder()
                .name(request.getName())
                .code(request.getCode())
                .registrationNumber(request.getRegistrationNumber())
                .taxNumber(request.getTaxNumber())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .email(request.getEmail())
                .phone(request.getPhone())
                .logoUrl(request.getLogoUrl())
                .status(Company.CompanyStatus.ACTIVE)
                .build();

        company = companyRepository.save(company);
        auditLogService.log("Company", company.getId().toString(), "CREATE", null, company);
        return mapToResponse(company);
    }

    public CompanyResponse updateCompany(UUID id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> BusinessException.notFound("Company"));

        Company oldState = cloneState(company);

        company.setName(request.getName());
        company.setRegistrationNumber(request.getRegistrationNumber());
        company.setTaxNumber(request.getTaxNumber());
        company.setAddress(request.getAddress());
        company.setCity(request.getCity());
        company.setCountry(request.getCountry());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setLogoUrl(request.getLogoUrl());

        company = companyRepository.save(company);
        auditLogService.log("Company", company.getId().toString(), "UPDATE", oldState, company);
        return mapToResponse(company);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .map(this::mapToResponse)
                .orElseThrow(() -> BusinessException.notFound("Company"));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .filter(c -> !c.isDeleted())
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteCompany(UUID id) {
        Company company = companyRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> BusinessException.notFound("Company"));

        company.setDeleted(true);
        companyRepository.save(company);
        auditLogService.log("Company", company.getId().toString(), "DELETE", null, null);
    }

    private CompanyResponse mapToResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setCode(company.getCode());
        response.setRegistrationNumber(company.getRegistrationNumber());
        response.setTaxNumber(company.getTaxNumber());
        response.setAddress(company.getAddress());
        response.setCity(company.getCity());
        response.setCountry(company.getCountry());
        response.setEmail(company.getEmail());
        response.setPhone(company.getPhone());
        response.setLogoUrl(company.getLogoUrl());
        response.setStatus(company.getStatus());
        response.setCreatedAt(company.getCreatedAt());
        return response;
    }

    private Company cloneState(Company source) {
        return Company.builder()
                .name(source.getName())
                .code(source.getCode())
                .registrationNumber(source.getRegistrationNumber())
                .taxNumber(source.getTaxNumber())
                .address(source.getAddress())
                .city(source.getCity())
                .country(source.getCountry())
                .email(source.getEmail())
                .phone(source.getPhone())
                .logoUrl(source.getLogoUrl())
                .status(source.getStatus())
                .build();
    }
}
