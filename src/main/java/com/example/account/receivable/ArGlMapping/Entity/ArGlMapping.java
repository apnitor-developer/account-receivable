package com.example.account.receivable.ArGlMapping.Entity;

import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.GLCodes.Entity.GlCode;
import com.example.account.receivable.User.entity.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table( name = "ar_gl_mappings" )
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArGlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ar_code_id", nullable = false)
    private ArCode arCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dr_gl_code_id", nullable = false)
    private GlCode debitGlCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cr_gl_code_id", nullable = false)
    private GlCode creditGlCode;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "is_active")
    private boolean isActive;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Users createdBy;

    @CreationTimestamp
    private Instant createdAt;
}

