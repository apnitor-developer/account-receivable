package com.example.account.receivable.Company.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.*;

public interface CompanyBankAccountRepository extends JpaRepository<CompanyBankAccount, Long> {
    List<CompanyBankAccount> findByCompanyId(Long companyId);
}
