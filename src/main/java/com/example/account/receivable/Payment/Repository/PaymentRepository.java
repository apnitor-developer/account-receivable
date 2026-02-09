package com.example.account.receivable.Payment.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;
import com.example.account.receivable.Payment.MonthlyPaymentProjection;
import com.example.account.receivable.Payment.PaymentMethodReportProjection;
import com.example.account.receivable.Payment.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    //Total Payment Received
    // TOTAL payments by company
    @Query("""
        SELECT COALESCE(SUM(p.paymentAmount), 0)
        FROM Payment p
        JOIN p.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
    """)
    BigDecimal getTotalPaymentsByCompany(@Param("companyId") Long companyId);

    // TODAY payments by company
    @Query("""
        SELECT COALESCE(SUM(p.paymentAmount), 0)
        FROM Payment p
        JOIN p.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
        AND p.paymentDate = :today
    """)
    BigDecimal getTodayPaymentsByCompany(
            @Param("companyId") Long companyId,
            @Param("today") LocalDate today
    );


    //Get Payments By the CompanyId
    @Query("""
        SELECT p
        FROM Payment p
        JOIN p.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
          AND c.deleted = false
    """)
    Page<Payment> findPaymentsByCompanyId(
            @Param("companyId") Long companyId,
            Pageable pageable
    );


    // Get Only the CREATED Payments 
    @Query("""
        SELECT DISTINCT p
        FROM Payment p
        JOIN p.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
        AND p.status = :status
        AND c.deleted = false
        AND (:fromDate IS NULL OR p.createdAt >= :fromDate)
        AND (:toDate IS NULL OR p.createdAt <= :toDate)
    """)
    Page<Payment> findPaymentsByCompanyStatusAndDateRange(
            @Param("companyId") Long companyId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    

    //Get Payments By the CompanyId (query used in the payment reports)
    @Query("""
        SELECT p
        FROM Payment p
        JOIN p.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
        AND c.deleted = false
        AND p.status = :status
        AND (:fromDate IS NULL OR p.paymentDate >= :fromDate)
        AND (:toDate IS NULL OR p.paymentDate <= :toDate)
    """)
    Page<Payment> findPaymentsByCompanyIdFilteredAndStatus(
            @Param("companyId") Long companyId,
            @Param("status") PaymentStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );



    //Query used to calculate payment reports(BANK_TRANSFER , CASH , UPI etc)
    @Query("""
        SELECT 
            p.paymentMethod AS paymentMethod,
            COUNT(p.id) AS count
        FROM Payment p
        JOIN p.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
        AND p.paymentDate >= :fromDate
        GROUP BY p.paymentMethod
        """)
    List<PaymentMethodReportProjection> getPaymentReport(
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate
    );


    //Query used to show the 12 month data used in the payment reports
    @Query("""
        SELECT 
            MONTH(p.paymentDate) AS month,
            COALESCE(SUM(p.paymentAmount), 0) AS total
        FROM Payment p
        JOIN p.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
        AND YEAR(p.paymentDate) = :year
        GROUP BY MONTH(p.paymentDate)
        ORDER BY MONTH(p.paymentDate)
        """)
    List<MonthlyPaymentProjection> getMonthlyPaymentsByYear(
            @Param("companyId") Long companyId,
            @Param("year") int year
    );


}