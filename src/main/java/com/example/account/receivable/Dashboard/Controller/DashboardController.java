package com.example.account.receivable.Dashboard.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Dashboard.DTO.DashboardSummaryResponse;
import com.example.account.receivable.Dashboard.Service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary() {

        ApiResponse<DashboardSummaryResponse> body = ApiResponse.successResponse(
            200, 
            "Data retreived successfully", 
            dashboardService.getDashboardSummary()
        );
        return ResponseEntity.status(200).body(body);
    }
}