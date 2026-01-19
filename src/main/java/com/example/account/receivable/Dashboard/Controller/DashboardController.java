package com.example.account.receivable.Dashboard.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Dashboard.DTO.CompanyInvoiceMonthlySeriesResponse;
import com.example.account.receivable.Dashboard.DTO.DashboardSummaryResponse;
import com.example.account.receivable.Dashboard.Service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary/company/{companyId}")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
        @PathVariable Long companyId
    ) {
        

        ApiResponse<DashboardSummaryResponse> body = ApiResponse.successResponse(
            200, 
            "Data retreived successfully", 
            dashboardService.getDashboardSummary(companyId)
        );
        return ResponseEntity.status(200).body(body);
    }


    //Calculate invoice monthly amount
    @GetMapping("/invoices/monthly/company/{companyId}")
    public ResponseEntity<ApiResponse<CompanyInvoiceMonthlySeriesResponse>> getMonthlyInvoiceTotals(
            @PathVariable Long companyId,
            @RequestParam(required = false) Integer year
    ) {
        CompanyInvoiceMonthlySeriesResponse data = dashboardService.getCompanyInvoiceMonthlySeries(companyId, year);

        ApiResponse<CompanyInvoiceMonthlySeriesResponse> body = ApiResponse.successResponse(
                200,
                "Monthly invoice totals retrieved successfully",
                data
        );
        return ResponseEntity.ok(body);
    }

}