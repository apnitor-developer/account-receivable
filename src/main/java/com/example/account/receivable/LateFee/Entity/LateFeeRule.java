package com.example.account.receivable.LateFee.Entity;

import java.math.BigDecimal;

import com.example.account.receivable.Company.Entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "late_fee_rules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LateFeeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "grace_period_days", nullable = false)
    private Integer gracePeriodDays;

    @Column(name = "late_fee_percentage", nullable = false)
    private BigDecimal lateFeePercentage;

    @Column(name = "mandatory_charge", nullable = false)
    private BigDecimal mandatoryCharge;

    private Boolean delated = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}
