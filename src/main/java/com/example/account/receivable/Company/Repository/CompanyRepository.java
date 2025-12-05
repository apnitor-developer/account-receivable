package com.example.account.receivable.Company.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.Company;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByLegalName(String legalName);
    boolean existsByCompanyCode(String companyCode);

        // ✅ only non-deleted for listing and fetching
    Page<Company> findByDeletedFalse(Pageable pageable);

    Optional<Company> findByIdAndDeletedFalse(Long companyId);
}






