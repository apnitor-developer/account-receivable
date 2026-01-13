package com.example.account.receivable.Payment.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    //Get Payments By the CompanyId (query used in the payment reports)
    @Query("""
        SELECT p
        FROM Payment p
        JOIN p.customer c
        JOIN c.companyCompanies cc
        WHERE cc.company.id = :companyId
        AND c.deleted = false
        AND (:fromDate IS NULL OR p.paymentDate >= :fromDate)
        AND (:toDate IS NULL OR p.paymentDate <= :toDate)
    """)
    Page<Payment> findPaymentsByCompanyIdFiltered(
            @Param("companyId") Long companyId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );


}