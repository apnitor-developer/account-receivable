package com.example.account.receivable.ArCalculation.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "company_month_end_balance"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyMonthEndBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // store "YYYY-MM"
    @Column(name = "year_month", nullable = false, length = 7)
    private String yearMonth;

    @Column(name = "as_of_date", nullable = false)
    private LocalDate asOfDate;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;

    @Column(name = "locked", nullable = false)
    private Boolean locked;
}
