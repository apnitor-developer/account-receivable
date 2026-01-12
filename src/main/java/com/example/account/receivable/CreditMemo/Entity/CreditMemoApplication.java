package com.example.account.receivable.CreditMemo.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.account.receivable.Invoice.Entity.Invoice;

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
@Table(name = "credit_memo_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditMemoApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_memo_id", nullable = false)
    private CreditMemo creditMemo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice; // nullable in requirement

    @Column(name = "applied_amount", nullable = false)
    private BigDecimal appliedAmount;

    @Column(name = "applied_date", nullable = false)
    private LocalDate appliedDate;
}

