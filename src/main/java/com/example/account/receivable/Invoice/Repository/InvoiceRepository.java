package com.example.account.receivable.Invoice.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice , Long> {
    Page<Invoice> findByDeletedFalse(Pageable pageable);

    List<Invoice> findByCustomerIdAndDeletedFalse(Long customerId);

    List<Invoice> findByCustomerId(Long customerId);

    // Get Invoices List based on the Status(OPEN , PARTIAL , PAID)
    List<Invoice> findByCustomerIdAndStatusIn(Long customerId, List<String> statuses);

    // Check if an invoice number already exists (manual or generated)
    boolean existsByInvoiceNumber(String invoiceNumber);

    // Get the last invoice like "INV-XXXX", ordered descending
    Optional<Invoice> findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(String prefix);
}
