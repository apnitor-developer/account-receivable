package com.example.account.receivable.WriteOff.Entity;


import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.WriteOff.StatusFile.WriteOffStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "invoice_write_offs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceWriteOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private WriteOffStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ar_code_id")
    private ArCode arCode;   // optional but recommended

    @Column(nullable = false)
    private LocalDate writeOffDate;

    @CreationTimestamp
    private Instant createdAt;
}

