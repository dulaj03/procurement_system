package com.procure.module.company.service;

import com.procure.common.exception.BusinessException;
import com.procure.module.company.dto.BranchDtos.BranchRequest;
import com.procure.module.company.dto.BranchDtos.BranchResponse;
import com.procure.module.company.entity.Branch;
import com.procure.module.company.entity.Company;
import com.procure.module.company.repository.BranchRepository;
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
public class BranchService {

    private final BranchRepository branchRepository;
    private final CompanyRepository companyRepository;
    private final AuditLogService auditLogService;

    public BranchResponse createBranch(UUID companyId, BranchRequest request) {
        Company company = companyRepository.findById(companyId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> BusinessException.notFound("Company"));

        if (branchRepository.existsByCompanyIdAndCodeAndDeletedFalse(companyId, request.getCode())) {
            throw BusinessException.conflict("Branch code already exists under this company: " + request.getCode());
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .city(request.getCity())
                .country(request.getCountry())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status(Branch.BranchStatus.ACTIVE)
                .company(company)
                .build();

        branch = branchRepository.save(branch);
        auditLogService.log("Branch", branch.getId().toString(), "CREATE", null, branch);
        return mapToResponse(branch);
    }

    public BranchResponse updateBranch(UUID companyId, UUID branchId, BranchRequest request) {
        Branch branch = branchRepository.findByIdAndCompanyIdAndDeletedFalse(branchId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Branch"));

        Branch oldState = cloneState(branch);

        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        branch.setCity(request.getCity());
        branch.setCountry(request.getCountry());
        branch.setPhone(request.getPhone());
        branch.setEmail(request.getEmail());

        branch = branchRepository.save(branch);
        auditLogService.log("Branch", branch.getId().toString(), "UPDATE", oldState, branch);
        return mapToResponse(branch);
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID companyId, UUID branchId) {
        return branchRepository.findByIdAndCompanyIdAndDeletedFalse(branchId, companyId)
                .map(this::mapToResponse)
                .orElseThrow(() -> BusinessException.notFound("Branch"));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getBranchesByCompany(UUID companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw BusinessException.notFound("Company");
        }
        return branchRepository.findByCompanyIdAndDeletedFalse(companyId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void deleteBranch(UUID companyId, UUID branchId) {
        Branch branch = branchRepository.findByIdAndCompanyIdAndDeletedFalse(branchId, companyId)
                .orElseThrow(() -> BusinessException.notFound("Branch"));

        branch.setDeleted(true);
        branchRepository.save(branch);
        auditLogService.log("Branch", branch.getId().toString(), "DELETE", null, null);
    }

    private BranchResponse mapToResponse(Branch branch) {
        BranchResponse response = new BranchResponse();
        response.setId(branch.getId());
        response.setName(branch.getName());
        response.setCode(branch.getCode());
        response.setAddress(branch.getAddress());
        response.setCity(branch.getCity());
        response.setCountry(branch.getCountry());
        response.setPhone(branch.getPhone());
        response.setEmail(branch.getEmail());
        response.setStatus(branch.getStatus());
        response.setCompanyId(branch.getCompany().getId());
        return response;
    }

    private Branch cloneState(Branch source) {
        return Branch.builder()
                .name(source.getName())
                .code(source.getCode())
                .address(source.getAddress())
                .city(source.getCity())
                .country(source.getCountry())
                .phone(source.getPhone())
                .email(source.getEmail())
                .status(source.getStatus())
                .build();
    }
}
