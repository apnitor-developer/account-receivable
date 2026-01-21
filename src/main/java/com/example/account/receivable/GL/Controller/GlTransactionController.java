package com.example.account.receivable.GL.Controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.GL.Dto.GlTransactionCreateRequest;
import com.example.account.receivable.GL.Dto.GlTransactionResponse;
import com.example.account.receivable.GL.Enum.GlReferenceType;
import com.example.account.receivable.GL.Service.GlTransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/gl/transactions")
@RequiredArgsConstructor
public class GlTransactionController {

    private final GlTransactionService glTransactionService;

    @PostMapping("/company/{companyId}/user/{userId}")
    public ResponseEntity<ApiResponse<GlTransactionResponse>> createTransaction(
        @PathVariable Long companyId,
        @Valid @RequestBody GlTransactionCreateRequest request
    ) {
        GlTransactionResponse response =
            glTransactionService.createTransaction(companyId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.successResponse(201, "GL transaction posted", response));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<GlTransactionResponse>>> getCompanyTransactions(
        @PathVariable Long companyId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) GlReferenceType referenceType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        Page<GlTransactionResponse> transactions = glTransactionService.getCompanyTransactions(
            companyId,
            page,
            size,
            referenceType,
            fromDate,
            toDate
        );

        return ResponseEntity.ok(
            ApiResponse.successResponse(200, "GL transactions fetched", transactions)
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<GlTransactionResponse>> getTransaction(
        @PathVariable Long transactionId
    ) {
        GlTransactionResponse response = glTransactionService.getTransaction(transactionId);

        return ResponseEntity.ok(
            ApiResponse.successResponse(200, "GL transaction fetched", response)
        );
    }
}
