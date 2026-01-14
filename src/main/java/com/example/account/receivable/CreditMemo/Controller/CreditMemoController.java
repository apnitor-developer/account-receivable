package com.example.account.receivable.CreditMemo.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.CreditMemo.Dto.AppliedCreditMemoOnInvoiceResponse;
import com.example.account.receivable.CreditMemo.Dto.CreateCreditMemoRequest;
import com.example.account.receivable.CreditMemo.Dto.CustomerCreditBalanceResponse;
import com.example.account.receivable.CreditMemo.Entity.CreditMemo;
import com.example.account.receivable.CreditMemo.Service.CreditMemoService;
import com.example.account.receivable.CreditMemo.StatusFile.CreditMemoStatus;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/credit-memos")
@RequiredArgsConstructor
public class CreditMemoController {

    private final CreditMemoService creditMemoService;

    // Create CreditMemo
    @PostMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<CreditMemo>> create(
            @PathVariable Long customerId,
            @RequestBody CreateCreditMemoRequest req
    ) {
        CreditMemo cm = creditMemoService.createCreditMemo(customerId, req);
        return ResponseEntity.status(201)
                .body(ApiResponse.successResponse(201, "Credit memo created", cm));
    }


    // Allow Credit Memo To Apply
    @PostMapping("/{creditMemoId}/approve")
    public ResponseEntity<ApiResponse<CreditMemo>> approveCreditMemo(
            @PathVariable Long creditMemoId
    ) {
        CreditMemo cm = creditMemoService.approveCreditMemo(creditMemoId);
        return ResponseEntity.ok(
                ApiResponse.successResponse(200, "Credit memo allowed", cm)
        );
    }



    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<CreditMemo>>> getCompanyCreditMemos(
                    @PathVariable Long companyId,
                    @RequestParam(required = false) CreditMemoStatus status,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "10") int size) {
            Page<CreditMemo> result = creditMemoService.getCompanyCreditMemosByStatus(companyId, status, page, size);

            return ResponseEntity.ok(
                            ApiResponse.successResponse(
                                            200,
                                            "Company credit memos retrieved successfully",
                                            result));
    }


    // Get customer credit balance
    @GetMapping("/customer/{customerId}/balance")
    public ResponseEntity<ApiResponse<CustomerCreditBalanceResponse>> balance(
            @PathVariable Long customerId
    ) {
        CustomerCreditBalanceResponse response =
                creditMemoService.getCustomerCreditBalance(customerId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Customer credit balance",
                        response
                )
        );
    }



    // Get Company Apply Credit Memos
    @GetMapping("/company/{companyId}/applications")
    public ResponseEntity<ApiResponse<Page<AppliedCreditMemoOnInvoiceResponse>>> getCompanyAppliedCreditMemos(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<AppliedCreditMemoOnInvoiceResponse> result =
                creditMemoService.getCompanyAppliedCreditMemos(companyId, page, size);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Company applied credit memos retrieved successfully",
                        result
                )
        );
    }

}

