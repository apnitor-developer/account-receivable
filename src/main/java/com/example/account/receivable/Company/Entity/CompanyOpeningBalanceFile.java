package com.example.account.receivable.Company.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "company_opening_balance_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyOpeningBalanceFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // which company this file belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    // raw CSV bytes
    @Lob
    @Column(name = "data")   // SQL Server -> varbinary(max)
    private byte[] data;

    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

