package com.example.account.receivable.ArCalculation.DTO;

import lombok.Data;

@Data
public class MonthEndRequest {
    private Long companyId;
    private int year;
}
