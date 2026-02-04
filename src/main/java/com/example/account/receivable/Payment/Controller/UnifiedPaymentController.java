package com.example.account.receivable.Payment.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Payment.Dto.UnifiedPaymentDto;
import com.example.account.receivable.Payment.Service.UnifiedPaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments/unified")
@RequiredArgsConstructor
public class UnifiedPaymentController {

    private final UnifiedPaymentService unifiedPaymentService;

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<UnifiedPaymentDto>>> getUnifiedPayments(
            @PathVariable Long companyId) {

        List<UnifiedPaymentDto> data =
                unifiedPaymentService.getCompanyPayments(companyId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Payments fetched successfully",
                        data
                )
        );
    }
}

