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

import com.example.account.receivable.Dashboard.CompanyInvoiceMonthProjection;
import com.example.account.receivable.Invoice.InvoiceAgingProjection;
import com.example.account.receivable.Invoice.InvoiceStatusProjection;
import com.example.account.receivable.Invoice.Entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice , Long> {
    Page<Invoice> findByDeletedFalse(Pageable pageable);

    List<Invoice> findByCustomerIdAndDeletedFalseAndStatusNotIn(Long customerId , List<String> statuses);

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

    @Query("""
        SELECT COALESCE(SUM(i.balanceDue), 0) - COALESCE(SUM(pa.appliedAmount), 0)
        FROM Invoice i
        LEFT JOIN PaymentApplication pa ON pa.invoice.id = i.id
        WHERE i.customer.id = :customerId
        AND i.deleted = false
        AND i.balanceDue > 0
        AND i.status IN ('OPEN', 'PARTIAL')
    """)
    BigDecimal getCustomerOutstandingBalance(@Param("customerId") Long customerId);



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
        AND (:dateFrom IS NULL OR i.invoiceDate >= :dateFrom)
        AND (:dateTo IS NULL OR i.invoiceDate <= :dateTo)
    """)
    Page<Invoice> findCompanyInvoicesByStatusAndDateRange(
            @Param("companyId") Long companyId,
            Pageable pageable,
            @Param("statuses") List<String> statuses,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
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



        //Query used to Get all invoices of a company that are : OPEN and PARTIAL , Overdue (dueDate < today) , balanceDue > 0 , Belong to customers of that company
        @Query("""
            SELECT 
                i.id,
                i.invoiceNumber,
                i.invoiceDate,
                i.dueDate,
                i.status,
                i.totalAmount,
                i.balanceDue,
                c.id,
                c.customerName
            FROM Invoice i
            JOIN i.customer c
            JOIN c.companyCompanies cc
            WHERE cc.company.id = :companyId
            AND i.deleted = false
            AND c.deleted = false
            AND i.balanceDue > 0
            AND i.status IN ('OPEN', 'PARTIAL')
            AND i.dueDate < CURRENT_DATE
        """)
        List<Object[]> findOverdueInvoicesByCompany(
            @Param("companyId") Long companyId
        );


        //Query used to get all the Invoices with filter(used for the Invoice Reports)
        @Query("""
            SELECT i
            FROM Invoice i
            JOIN i.customer c
            JOIN c.companyCompanies cc
            WHERE cc.company.id = :companyId
            AND i.deleted = false
            AND c.deleted = false
            AND (:statuses IS NULL OR i.status IN :statuses)
            AND (:fromDate IS NULL OR i.invoiceDate >= :fromDate)
            AND (:toDate IS NULL OR i.invoiceDate <= :toDate)
        """)
        Page<Invoice> findCompanyInvoicesFiltered(
                @Param("companyId") Long companyId,
                @Param("statuses") List<String> statuses,
                @Param("fromDate") LocalDate fromDate,
                @Param("toDate") LocalDate toDate,
                Pageable pageable
        );



        //Query Used to  Cal Invoice Reports(CURRENT , 0-30 , 30-60 , 60-90 , 90>)
        @Query("""
            SELECT
                SUM(CASE WHEN i.dueDate >= :today THEN 1 ELSE 0 END) AS current,
                SUM(CASE WHEN i.dueDate < :today AND i.dueDate >= :todayMinus30 THEN 1 ELSE 0 END) AS days0to30,
                SUM(CASE WHEN i.dueDate < :todayMinus30 AND i.dueDate >= :todayMinus60 THEN 1 ELSE 0 END) AS days31to60,
                SUM(CASE WHEN i.dueDate < :todayMinus60 AND i.dueDate >= :todayMinus90 THEN 1 ELSE 0 END) AS days61to90,
                SUM(CASE WHEN i.dueDate < :todayMinus90 THEN 1 ELSE 0 END) AS days90Plus
            FROM Invoice i
            JOIN i.customer c
            JOIN c.companyCompanies cc
            WHERE cc.company.id = :companyId
            AND i.deleted = false
            AND c.deleted = false
            AND i.balanceDue > 0
            AND i.status IN ('OPEN','PARTIAL')
            """)
        InvoiceAgingProjection getInvoiceAgingReport(
                @Param("companyId") Long companyId,
                @Param("today") LocalDate today,
                @Param("todayMinus30") LocalDate todayMinus30,
                @Param("todayMinus60") LocalDate todayMinus60,
                @Param("todayMinus90") LocalDate todayMinus90
        );



        //Query Used to  Cal Invoice Reports(OPEN ,PARTIAL , PAID , WRITTEN_OFF)
        @Query("""
            SELECT
                SUM(CASE WHEN i.status = 'OPEN' THEN 1 ELSE 0 END) AS open,
                SUM(CASE WHEN i.status = 'PARTIAL' THEN 1 ELSE 0 END) AS partial,
                SUM(CASE WHEN i.status = 'PAID' THEN 1 ELSE 0 END) AS paid,
                SUM(CASE WHEN i.status = 'WRITTEN_OFF' THEN 1 ELSE 0 END) AS writtenOff
            FROM Invoice i
            JOIN i.customer c
            JOIN c.companyCompanies cc
            WHERE cc.company.id = :companyId
            AND i.deleted = false
            AND c.deleted = false
            AND i.invoiceDate >= :fromDate
            """)
        InvoiceStatusProjection getInvoiceStatusBreakdown(
                @Param("companyId") Long companyId,
                @Param("fromDate") LocalDate fromDate
        );



    //Query used to calculate the invoice amount per month(used for the graph)
    @Query(value = """
        SELECT
        TO_CHAR(i.INVOICE_DATE, 'YYYY-MM') AS yearMonth,
        COUNT(*) AS invoiceCount,
        NVL(SUM(i.TOTAL_AMOUNT), 0) AS totalAmount
        FROM APNITOR.INVOICES i
        JOIN APNITOR.CUSTOMER c ON c.ID = i.CUSTOMER_ID
        JOIN APNITOR.COMPANY_CUSTOMERS cc ON cc.CUSTOMER_ID = c.ID
        WHERE cc.COMPANY_ID = :companyId
        AND i.DELETED = 0
        AND c.DELETED = 0
        AND i.INVOICE_DATE >= :fromDate
        AND i.INVOICE_DATE <= :toDate
        GROUP BY TO_CHAR(i.INVOICE_DATE, 'YYYY-MM')
        ORDER BY yearMonth
    """, nativeQuery = true)
    List<CompanyInvoiceMonthProjection> getCompanyInvoiceMonthlyTotals(
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );



}
