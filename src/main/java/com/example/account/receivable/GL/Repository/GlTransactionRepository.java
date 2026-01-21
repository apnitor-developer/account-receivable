package com.example.account.receivable.GL.Repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.GL.Entity.GlTransaction;
import com.example.account.receivable.GL.Enum.GlReferenceType;

public interface GlTransactionRepository extends JpaRepository<GlTransaction, Long> {

    @Query("""
        SELECT t
        FROM GlTransaction t
        WHERE t.company.id = :companyId
        AND (:referenceType IS NULL OR t.referenceType = :referenceType)
        AND (:fromDate IS NULL OR t.transactionDate >= :fromDate)
        AND (:toDate IS NULL OR t.transactionDate <= :toDate)
        ORDER BY t.transactionDate DESC, t.id DESC
    """)
    Page<GlTransaction> findCompanyTransactions(
        @Param("companyId") Long companyId,
        @Param("referenceType") GlReferenceType referenceType,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    Optional<GlTransaction> findByIdAndCompany_Id(Long id, Long companyId);

    Optional<GlTransaction> findByReferenceTypeAndReferenceIdAndCompany_Id(
        GlReferenceType referenceType,
        Long referenceId,
        Long companyId
    );
}
