package com.example.account.receivable.Invoice.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.receivable.Invoice.Entity.RecurringInvoiceTemplate;

@Repository
public interface RecurringInvoiceTemplateRepository
        extends JpaRepository<RecurringInvoiceTemplate, Long> {

    List<RecurringInvoiceTemplate> findByActiveTrue();
}
