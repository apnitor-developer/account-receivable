package com.example.account.receivable.Aging.DTO;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAgingDto {
    
    private Long customerId;
    private String customerName;

    private BigDecimal totalDue;
    private BigDecimal current;
    private BigDecimal bucket1To30;
    private BigDecimal bucket31To60;
    private BigDecimal bucket61To90;
    private BigDecimal bucketGt90;
}
