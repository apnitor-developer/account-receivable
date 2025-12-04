package com.example.account.receivable.Company.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.CompanyPaymentSettings;

import java.util.Optional;

public interface CompanyPaymentSettingsRepository
        extends JpaRepository<CompanyPaymentSettings, Long> {

    // find by company.id
    Optional<CompanyPaymentSettings> findByCompany_Id(Long companyId);
}


