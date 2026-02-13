package com.example.account.receivable.PaymentTerms.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.PaymentTerms.Dto.CreatePaymentTermRequest;
import com.example.account.receivable.PaymentTerms.Dto.PaymentTermResponseDto;
import com.example.account.receivable.PaymentTerms.Dto.UpdatePaymentTermRequest;
import com.example.account.receivable.PaymentTerms.Service.PaymentTermService;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
public class PaymentTermController {

    private final PaymentTermService paymentTermService;

    // CREATE
    @PostMapping("/{companyId}/payment-terms")
    public ResponseEntity<ApiResponse<PaymentTermResponseDto>> createPaymentTerm(
            @PathVariable Long companyId,
            @RequestBody CreatePaymentTermRequest request) {

        PaymentTermResponseDto response =
                paymentTermService.createPaymentTerm(companyId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.successResponse(
                        HttpStatus.CREATED.value(),
                        "Payment term created successfully",
                        response
                ));
    }

    // GET GLOBAL PAYMENT TERMS
    @GetMapping("/global")
    public ResponseEntity<ApiResponse<List<PaymentTermResponseDto>>> getGlobalPaymentTerms() {

        List<PaymentTermResponseDto> response =
                paymentTermService.getGlobalPaymentTerms();

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Global payment terms retrieved successfully",
                        response
                )
        );
    }

    // GET Company Payment Terms
    @GetMapping("/{companyId}/payment-terms")
    public ResponseEntity<ApiResponse<List<PaymentTermResponseDto>>> getAllPaymentTerms(
            @PathVariable Long companyId) {

        List<PaymentTermResponseDto> response =
                paymentTermService.getAllPaymentTerms(companyId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Payment terms retrieved successfully",
                        response
                )
        );
    }

    // GET BY ID
    @GetMapping("/{companyId}/payment-terms/{paymentTermId}")
    public ResponseEntity<ApiResponse<PaymentTermResponseDto>> getPaymentTermById(
            @PathVariable Long companyId,
            @PathVariable Long paymentTermId) {

        PaymentTermResponseDto response =
                paymentTermService.getPaymentTermById(companyId, paymentTermId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Payment term retrieved successfully",
                        response
                )
        );
    }

    // UPDATE
    @PutMapping("/{companyId}/payment-terms/{paymentTermId}")
    public ResponseEntity<ApiResponse<PaymentTermResponseDto>> updatePaymentTerm(
            @PathVariable Long companyId,
            @PathVariable Long paymentTermId,
            @RequestBody UpdatePaymentTermRequest request) {

        PaymentTermResponseDto response =
                paymentTermService.updatePaymentTerm(companyId, paymentTermId, request);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Payment term updated successfully",
                        response
                )
        );
    }

    // DELETE
    @DeleteMapping("/{companyId}/payment-terms/{paymentTermId}")
    public ResponseEntity<ApiResponse<String>> deletePaymentTerm(
            @PathVariable Long companyId,
            @PathVariable Long paymentTermId) {

        paymentTermService.deletePaymentTerm(companyId, paymentTermId);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Payment term deleted successfully",
                        "Deleted"
                )
        );
    }
}
