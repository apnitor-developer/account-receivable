package com.example.account.receivable.BankReconciliation.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {
}
