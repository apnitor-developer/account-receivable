package com.example.account.receivable.CreditMemo.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.CreditMemo.StatusFile.CreditMemoStatus;
import com.example.account.receivable.Customer.Entity.Customer;

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
@Table(name = "credit_memos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    // @JsonIgnore
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "credit_memo_no", nullable = false, unique = true, length = 32)
    private String creditMemoNo;

    @Column(name = "credit_reason")
    private String creditReason;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditMemoStatus status; // keep it for schema compatibility ("Posted")

    @Column(name = "posting_date")
    private LocalDate postingDate;

    // 👇 NEW (optional invoice selection at draft time)
    @Column(name = "target_invoice_id")
    private Long targetInvoiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    // @JsonIgnore
    @JoinColumn(name = "ar_code_id")
    private ArCode arCode;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

