package com.example.account.receivable.Aging.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.account.receivable.Aging.DTO.AgingReportResponse;
import com.example.account.receivable.Aging.Service.AgingReportService;
import com.example.account.receivable.Common.ApiResponse;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports/aging")
@RequiredArgsConstructor
public class AgingReportController {

    private final AgingReportService agingReportService;

    @GetMapping
    public ResponseEntity<ApiResponse<AgingReportResponse>> getAgingReport(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate
    ) {
        AgingReportResponse response = agingReportService.getAgingReport(asOfDate, customerId, status);

        ApiResponse<AgingReportResponse> body = ApiResponse.successResponse(
            200, 
            "Get Aging and Reports successfully", 
            response
        );
        return ResponseEntity.status(201).body(body);
    }
}