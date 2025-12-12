package com.example.account.receivable.Invoice.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    //For calculating the Aging
    List<Invoice> findByActiveTrueAndDeletedFalseAndBalanceDueGreaterThan(BigDecimal balanceDue);

    //Use this function to calculate the allbalance dues of the invoices
    List<Invoice> findByCustomerIdAndBalanceDueGreaterThan(Long customerId, BigDecimal balance);



    //Calculate Total Pending Amount of the company

    @Query("""
        SELECT COALESCE(SUM(i.balanceDue), 0)
        FROM Invoice i
        JOIN i.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
          AND i.balanceDue > 0
          AND c.deleted = false
          AND i.dueDate <= CURRENT_DATE
    """)
    BigDecimal getCompanyTotalPendingAmount(@Param("companyId") Long companyId);

}
