package com.example.account.receivable.Aging.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgingReportResponse {

    private LocalDate asOfDate;
    private List<CustomerAgingDto> rows;
}
