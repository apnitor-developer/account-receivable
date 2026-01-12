package com.example.account.receivable.CreditMemo.Repository;

import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.CreditMemo.Entity.CreditMemoApplication;

public interface CreditMemoApplicationRepository extends JpaRepository<CreditMemoApplication, Long> {

    @Query("""
        SELECT COALESCE(SUM(a.appliedAmount), 0)
        FROM CreditMemoApplication a
        WHERE a.creditMemo.id = :creditMemoId
    """)
    BigDecimal getAppliedTotal(@Param("creditMemoId") Long creditMemoId);

    @Query("""
        SELECT COALESCE(SUM(a.appliedAmount), 0)
        FROM CreditMemoApplication a
        WHERE a.creditMemo.customer.id = :customerId
    """)
    BigDecimal getCustomerTotalApplied(@Param("customerId") Long customerId);


    @Query("""
        SELECT a
        FROM CreditMemoApplication a
        JOIN FETCH a.creditMemo cm
        JOIN FETCH a.invoice inv
        JOIN FETCH inv.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        LEFT JOIN FETCH cm.arCode ac
        WHERE cc.company.id = :companyId
        ORDER BY a.appliedDate DESC
    """)
    Page<CreditMemoApplication> findCompanyAppliedCreditMemos(
            @Param("companyId") Long companyId,
            Pageable pageable
    );
}
