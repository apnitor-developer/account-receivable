package com.example.account.receivable.PaymentTerms.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.PaymentTerms.Entity.PaymentTerm;

public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {

    boolean existsByNameIgnoreCaseAndCompanyId(String name, Long companyId);

    List<PaymentTerm> findByCompanyIdAndActiveTrue(Long companyId);

    Optional<PaymentTerm> findByIdAndCompanyId(Long id, Long companyId);

    // Global Terms
    List<PaymentTerm> findByCompanyIsNullAndActiveTrue();
}