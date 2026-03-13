package com.example.account.receivable.AgingReports.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "aging_bucket")
@Data
public class AgingBucket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private String bucketName;

    private Integer startDay;

    private Integer endDay;

    private Integer displayOrder;

    private Boolean delated = false;

    private Boolean active = true;
}
