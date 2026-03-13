package com.example.account.receivable.AgingReports.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AgingCodeResponse {

    private Long id;
    private String bucketName;
    private Integer startDay;
    private Integer endDay;
    private Integer displayOrder;
}
