package com.procure.module.company.repository;

import com.procure.module.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByCodeAndDeletedFalse(String code);
    boolean existsByCode(String code);
    boolean existsByRegistrationNumber(String registrationNumber);
}
