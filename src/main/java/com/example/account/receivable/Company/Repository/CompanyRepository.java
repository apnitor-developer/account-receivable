package com.example.account.receivable.Company.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.Company.Entity.Company;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByLegalName(String legalName);
    boolean existsByCompanyCode(String companyCode);

    Page<Company> findByDeletedFalse(Pageable pageable);

    Optional<Company> findByIdAndDeletedFalse(Long companyId);

    // Custom query to fetch companies by userId
    @Query("SELECT c FROM Company c JOIN c.userCompanies uc WHERE uc.user.id = :userId AND c.deleted = false")
    Page<Company> findByUserIdAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);

    @Query("select c.id from Company c where c.deleted = false")
    List<Long> findActiveCompanyIds();
}






