package com.example.account.receivable.BankAccount_GlMapping.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.receivable.BankAccount_GlMapping.Entity.BankAccountGlMapping;
import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;

@Repository
public interface BankAccountGlMappingRepository extends JpaRepository<BankAccountGlMapping, Long> {

    Optional<BankAccountGlMapping>
        findByBankAccount_IdAndStatus(
            Long bankAccountId,
            MappingStatus status
        );

    Optional<BankAccountGlMapping>
        findByIdAndCompany_Id(Long id, Long companyId);
}

