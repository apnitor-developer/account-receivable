package com.example.account.receivable.ArCalculation.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.ArCalculation.Entity.CompanyYearEndClose;

public interface CompanyYearCloseRepository
        extends JpaRepository<CompanyYearEndClose, Long> {

    boolean existsByCompanyIdAndYearAndClosedTrue(Long companyId, Integer year);
}
