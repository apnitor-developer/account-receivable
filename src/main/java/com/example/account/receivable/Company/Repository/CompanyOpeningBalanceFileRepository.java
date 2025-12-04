package com.example.account.receivable.Company.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.CompanyOpeningBalanceFile;

import java.util.Optional;

public interface CompanyOpeningBalanceFileRepository
        extends JpaRepository<CompanyOpeningBalanceFile, Long> {

    // latest file per company (if you ever need it)
    Optional<CompanyOpeningBalanceFile> findFirstByCompany_IdOrderByUploadedAtDesc(Long companyId);

    void deleteByCompany_Id(Long companyId);   // if you want one file per company
}

