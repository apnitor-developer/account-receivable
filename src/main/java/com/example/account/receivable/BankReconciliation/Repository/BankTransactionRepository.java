package com.example.account.receivable.BankReconciliation.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
    List<BankTransaction> findByCompany_Id(Long companyId);
}
