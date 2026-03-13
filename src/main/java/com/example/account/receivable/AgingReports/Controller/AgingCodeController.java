package com.example.account.receivable.AgingReports.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.AgingReports.DTO.AgingCodeResponse;
import com.example.account.receivable.AgingReports.DTO.CreateAgingCodeRequest;
import com.example.account.receivable.AgingReports.Service.AgingReportService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/aging-codes")
@RequiredArgsConstructor
public class AgingCodeController {
    private final AgingReportService agingReportService;

    @PostMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<AgingCodeResponse>> createAgingCode(
            @PathVariable("companyId") Long companyId,
            @RequestBody CreateAgingCodeRequest request
    ) {

        AgingCodeResponse response = agingReportService.createAgingCode(companyId, request);

        ApiResponse<AgingCodeResponse> body = ApiResponse.successResponse(
                201,
                "Aging Code created successfully",
                response
        );

        return ResponseEntity.status(201).body(body);
    }



    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<AgingCodeResponse>>> getAgingCodes(
            @PathVariable("companyId") Long companyId
    ) {

        List<AgingCodeResponse> response = agingReportService.getAgingCodes(companyId);

        ApiResponse<List<AgingCodeResponse>> body = ApiResponse.successResponse(
                200,
                "Aging Codes fetched successfully",
                response
        );

        return ResponseEntity.status(200).body(body);
    }



    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgingCodeResponse>> updateAgingCode(
            @PathVariable("id") Long id,
            @RequestBody CreateAgingCodeRequest request
    ) {

        AgingCodeResponse response = agingReportService.updateAgingCode(id, request);

        ApiResponse<AgingCodeResponse> body = ApiResponse.successResponse(
                200,
                "Aging Code updated successfully",
                response
        );

        return ResponseEntity.status(200).body(body);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAgingCode(
            @PathVariable("id") Long id
    ) {

        agingReportService.deleteAgingCode(id);

        ApiResponse<String> body = ApiResponse.successResponse(
                200,
                "Aging Code deleted successfully",
                null
        );

        return ResponseEntity.status(200).body(body);
    }
}
