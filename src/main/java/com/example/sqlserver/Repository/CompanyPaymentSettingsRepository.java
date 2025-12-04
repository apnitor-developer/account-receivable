package com.example.sqlserver.Repository;

import org.springframework.data.jpa.repository.JpaRepository;


import com.example.sqlserver.Entity.CompanyPaymentSettings;

import java.util.Optional;

public interface CompanyPaymentSettingsRepository
        extends JpaRepository<CompanyPaymentSettings, Long> {

    // find by company.id
    Optional<CompanyPaymentSettings> findByCompany_Id(Long companyId);
}


