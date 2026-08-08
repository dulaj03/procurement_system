package com.procure.module.company.repository;

import com.procure.module.company.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    List<Branch> findByCompanyIdAndDeletedFalse(UUID companyId);
    Optional<Branch> findByIdAndCompanyIdAndDeletedFalse(UUID id, UUID companyId);
    boolean existsByCompanyIdAndCodeAndDeletedFalse(UUID companyId, String code);
}
