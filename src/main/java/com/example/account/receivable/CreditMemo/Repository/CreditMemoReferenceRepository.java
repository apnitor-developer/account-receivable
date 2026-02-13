package com.example.account.receivable.CreditMemo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.CreditMemo.Entity.CreditMemoReference;

public interface CreditMemoReferenceRepository
        extends JpaRepository<CreditMemoReference, Long> {
}
