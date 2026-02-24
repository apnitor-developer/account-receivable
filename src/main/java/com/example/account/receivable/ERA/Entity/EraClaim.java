package com.example.account.receivable.ERA.Entity;


import java.math.BigDecimal;
import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "era_claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EraClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "era_batch_id")
    private EraBatch batch;

    private String claimNumber;      // CLP01
    private String invoiceNumber;    // CLP07

    private BigDecimal billedAmount; // CLP03
    private BigDecimal paidAmount;   // CLP04
    private BigDecimal allowedAmount; // AMT*AU

    private BigDecimal patientResponsibility; // PR total
    private BigDecimal contractualAmount;     // CO total

    private String rawClpSegment;

    @CreationTimestamp
    private Instant createdAt;
}
