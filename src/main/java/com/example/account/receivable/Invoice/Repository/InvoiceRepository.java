package com.example.account.receivable.Invoice.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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


    long countByDeletedFalse();

    long countByBalanceDueGreaterThan(BigDecimal amount);


    // These queries used in the Dashboard
        // TOTAL receivables by company
        @Query("""
            SELECT COALESCE(SUM(i.balanceDue), 0)
            FROM Invoice i
            JOIN i.customer c
            JOIN CompanyCustomers cc ON cc.customer = c
            WHERE cc.company.id = :companyId
            AND i.balanceDue > 0
            AND i.deleted = false
        """)
        BigDecimal getTotalReceivablesByCompany(@Param("companyId") Long companyId);

        // CURRENT receivables by company
        @Query("""
            SELECT COALESCE(SUM(i.balanceDue), 0)
            FROM Invoice i
            JOIN i.customer c
            JOIN CompanyCustomers cc ON cc.customer = c
            WHERE cc.company.id = :companyId
            AND i.balanceDue > 0
            AND i.deleted = false
            AND i.dueDate >= :today
        """)
        BigDecimal getCurrentReceivablesByCompany(
                @Param("companyId") Long companyId,
                @Param("today") LocalDate today
        );

        // TOTAL invoices by company
        @Query("""
            SELECT COUNT(i)
            FROM Invoice i
            JOIN i.customer c
            JOIN CompanyCustomers cc ON cc.customer = c
            WHERE cc.company.id = :companyId
            AND i.deleted = false
        """)
        long countByCompanyAndDeletedFalse(@Param("companyId") Long companyId);
        

    // PENDING invoices by company
    @Query("""
        SELECT COUNT(i)
        FROM Invoice i
        JOIN i.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
        AND i.balanceDue > 0
    """)
    long countPendingByCompany(@Param("companyId") Long companyId);


    //Get Invoices By the CompanyId
    @Query("""
        SELECT i
        FROM Invoice i
        JOIN i.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
        AND i.deleted = false
        AND c.deleted = false
        AND i.status IN :statuses
    """)
    Page<Invoice> findCompanyInvoicesByStatus(
            @Param("companyId") Long companyId,
            Pageable pageable,
            @Param("statuses") List<String> statuses
    );


    //Query used for the Aging
    @Query("""
        SELECT DISTINCT i
        FROM Invoice i
        JOIN i.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
            AND i.active = true
            AND i.deleted = false
            AND i.balanceDue > 0
    """)
    List<Invoice> findOpenInvoicesByCompany(@Param("companyId") Long companyId);



    //Query used for the ArCalculation

        //Customer Month-End Balance
        @Query("""
            SELECT COALESCE(SUM(i.balanceDue), 0)
            FROM Invoice i
            WHERE i.customer.id = :customerId
                AND i.invoiceDate <= :asOfDate
                AND i.deleted = false
        """)
        BigDecimal getCustomerMonthEndBalance(
                @Param("customerId") Long customerId,
                @Param("asOfDate") LocalDate asOfDate
        );


        // Company Month-End Balance
        @Query("""
            SELECT COALESCE(SUM(i.balanceDue), 0)
            FROM Invoice i
            JOIN i.customer c
            JOIN CompanyCustomers cc ON cc.customer = c
            WHERE cc.company.id = :companyId
            AND i.invoiceDate <= :asOfDate
            AND i.deleted = false
            AND c.deleted = false
        """)
        BigDecimal getCompanyMonthEndBalance(
                @Param("companyId") Long companyId,
                @Param("asOfDate") LocalDate asOfDate
        );

}
