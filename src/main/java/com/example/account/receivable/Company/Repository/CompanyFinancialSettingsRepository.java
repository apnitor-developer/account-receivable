package com.example.account.receivable.Company.Repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.CompanyFinancialSettings;

public interface CompanyFinancialSettingsRepository
        extends JpaRepository<CompanyFinancialSettings, Long> {

    // find by company.id
    Optional<CompanyFinancialSettings> findByCompany_Id(Long companyId);
}

