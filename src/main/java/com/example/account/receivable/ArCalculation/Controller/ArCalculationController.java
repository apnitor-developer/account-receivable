package com.example.account.receivable.ArCalculation.Controller;

import java.time.YearMonth;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.ArCalculation.DTO.CompanyBalanceSeriesDto;
import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceFullDto;
import com.example.account.receivable.ArCalculation.DTO.CustomerMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.DTO.MonthEndRequest;
import com.example.account.receivable.ArCalculation.Service.CompanyBalanceCalculator;
import com.example.account.receivable.ArCalculation.Service.CustomerBalanceCalculator;
import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Dashboard.Service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ar")
@RequiredArgsConstructor
public class ArCalculationController {

    private final CompanyBalanceCalculator companyBalanceCalculator;
    private final CustomerBalanceCalculator customerBalanceCalculator;
    private final DashboardService dashboardService;

    //Get Monthend only for one month
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

    //Get Monthend for one Year
    @PostMapping("/company/year-full")
    public ResponseEntity<ApiResponse<List<CompanyMonthEndBalanceFullDto>>> getFullYearData(
            @RequestBody MonthEndRequest request

    ) {

        List<CompanyMonthEndBalanceFullDto> data =
                companyBalanceCalculator.getYearlyBalances(request);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Full yearly data retrieved",
                        data
                )
        );
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


    @GetMapping("/company/{companyId}/balance-series")
    public ResponseEntity<ApiResponse<CompanyBalanceSeriesDto>> getBalanceSeries(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "12") int months
    ) {
        ApiResponse<CompanyBalanceSeriesDto> body = ApiResponse.successResponse(
                200,
                "Graph data retrieved successfully",
                dashboardService.getCompanyBalanceSeries(companyId, months)
        );
        return ResponseEntity.ok(body);
    }

}

