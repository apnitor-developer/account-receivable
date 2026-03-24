package com.example.account.receivable.ArCalculation.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "company_year_end_close",
       uniqueConstraints = @UniqueConstraint(columnNames = {"company_id","year"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyYearEndClose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private Integer year;

    private Boolean closed;

    private LocalDateTime closedAt;
}