package com.example.account.receivable.AgingReports.DTO;

import lombok.Data;

@Data
public class CreateAgingCodeRequest {

    private String bucketName;
    private Integer startDay;
    private Integer endDay;
    private Integer displayOrder;
}
