package com.example.account.receivable.WriteOff.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.WriteOff.Entity.InvoiceWriteOff;

public interface InvoiceWriteOffRepository
        extends JpaRepository<InvoiceWriteOff, Long> {

    @Query("""
        SELECT w
        FROM InvoiceWriteOff w
        WHERE w.company.id = :companyId
        ORDER BY w.createdAt DESC
    """)
    Page<InvoiceWriteOff> findCompanyWriteOffs(
            @Param("companyId") Long companyId,
            Pageable pageable
    );
}
