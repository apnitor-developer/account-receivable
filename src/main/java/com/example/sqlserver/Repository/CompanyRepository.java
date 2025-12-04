package com.example.sqlserver.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.sqlserver.Entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByLegalName(String legalName);
    boolean existsByCompanyCode(String companyCode);

        // ✅ only non-deleted for listing and fetching
    Page<Company> findByDeletedFalse(Pageable pageable);

    Optional<Company> findByIdAndDeletedFalse(Long id);
}






