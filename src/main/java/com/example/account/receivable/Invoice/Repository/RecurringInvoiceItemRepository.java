package com.example.account.receivable.Invoice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.receivable.Invoice.Entity.RecurringInvoiceItem;

@Repository
public interface RecurringInvoiceItemRepository
        extends JpaRepository<RecurringInvoiceItem, Long> {
}
