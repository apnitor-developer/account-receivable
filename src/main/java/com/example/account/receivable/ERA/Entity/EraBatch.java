package com.example.account.receivable.ERA.Entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;

import com.example.account.receivable.ERA.Enum.EraStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "era_batches")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EraBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String payerName;        // N1*PR
    private String traceNumber;      // TRN02
    private BigDecimal totalPayment; // BPR02
    private LocalDate paymentDate;   // BPR16

    private String rawFileName;

    @Enumerated(EnumType.STRING)
    private EraStatus status;

    @CreationTimestamp
    private Instant createdAt;
}
