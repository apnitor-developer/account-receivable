package com.example.sqlserver.Repository;

import com.example.sqlserver.Entity.*;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Long> {
    List<CompanyBankAccount> findByCompanyId(Long companyId);
}
