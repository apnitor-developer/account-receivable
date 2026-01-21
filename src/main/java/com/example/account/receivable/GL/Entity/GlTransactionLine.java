package com.example.account.receivable.GL.Entity;

import java.math.BigDecimal;

import com.example.account.receivable.GL.Enum.GlEntryType;
import com.fasterxml.jackson.annotation.JsonIgnore;

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
@Table(name = "gl_transaction_lines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlTransactionLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private GlTransaction transaction;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "gl_code_id", nullable = false)
    // private GlCode glCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private GlEntryType entryType;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;

    private String narration;
}
