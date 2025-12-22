package com.example.account.receivable.ArCalculation.Controller;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.DTO.CustomerMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.Service.CompanyBalanceCalculator;
import com.example.account.receivable.ArCalculation.Service.CustomerBalanceCalculator;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ar")
@RequiredArgsConstructor
public class ArCalculationController {

    private final CompanyBalanceCalculator companyBalanceCalculator;
    private final CustomerBalanceCalculator customerBalanceCalculator;

    @GetMapping("/company/month-end")
    public  ResponseEntity<ApiResponse<CompanyMonthEndBalanceDto>> getCompanyMonthEnd(
            @RequestParam Long companyId,
            @RequestParam String month   // "2025-01"
    ) {
        YearMonth yearMonth = YearMonth.parse(month);
        ApiResponse<CompanyMonthEndBalanceDto> body = ApiResponse.successResponse(
            200, 
            "Data retreived successfully", 
            companyBalanceCalculator.calculate(companyId, yearMonth)
        );
        return ResponseEntity.status(200).body(body);
    }

    
    @GetMapping("/customer/month-end")
    public ResponseEntity<ApiResponse<CustomerMonthEndBalanceDto>> getCustomerMonthEnd(
            @RequestParam Long customerId,
            @RequestParam String month
    ) {
        YearMonth yearMonth = YearMonth.parse(month);
        ApiResponse<CustomerMonthEndBalanceDto> body = ApiResponse.successResponse(
            200, 
            "Data retreived successfully", 
            customerBalanceCalculator.calculate(customerId, yearMonth)
        );
        return ResponseEntity.status(200).body(body);
    }
}

