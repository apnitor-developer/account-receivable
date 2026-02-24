package com.example.account.receivable.BankReconciliation.Controller;

import java.time.LocalDate;
import java.util.List;

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
import org.springframework.web.multipart.MultipartFile;

import com.example.account.receivable.BankReconciliation.Dto.BankApproveApplyRequest;
import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;
import com.example.account.receivable.BankReconciliation.Service.BankReconciliationService;
import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Payment.Entity.Payment;


import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/bank-reconciliation")
@RequiredArgsConstructor
public class BankReconciliationController {

    private final BankReconciliationService bankReconciliationService;

    // Upload BAI file
    @PostMapping("/company/{companyId}/upload")
    public ResponseEntity<ApiResponse<String>> uploadBaiFile(
                @PathVariable() Long companyId,
                @RequestParam("file") MultipartFile file) {

        bankReconciliationService.processBaiFile(file , companyId);

        ApiResponse<String> response =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "BAI file processed successfully",
                        "SUCCESS"
                );

        return ResponseEntity.ok(response);
    }


    // Approve and Apply on Invoice
    @PostMapping("/transaction/{bankTransactionId}/approve-apply")
    public ResponseEntity<ApiResponse<Payment>> approveAndApply(
                    @PathVariable Long bankTransactionId,
                    @RequestBody BankApproveApplyRequest request) {
            Payment payment = bankReconciliationService.approveAndApplyBankTransaction(
                            bankTransactionId,
                            request.getCustomerId(),
                            request.getInvoiceIds());

            return ResponseEntity.ok(
                            ApiResponse.successResponse(
                                            200,
                                            "Bank transaction approved and applied",
                                            payment));
    }


    @PostMapping("/company/{companyId}/transaction/{bankTransactionId}/approve-with-era")
    public ResponseEntity<ApiResponse<String>> approveWithEra(
                    @PathVariable Long bankTransactionId,
                    @PathVariable Long companyId) {

            bankReconciliationService.approveWithEraAndCreatePatientInvoice(bankTransactionId, companyId);

            return ResponseEntity.ok(
                            ApiResponse.successResponse(
                                            200,
                                            "Bank transaction approved using ERA",
                                            "SUCCESS"));
    }


    
    //Get Bank Transaction List
    @GetMapping("/company/{companyId}/transactions")
    public ResponseEntity<ApiResponse<List<BankTransaction>>> getBankTransactions(
                    @PathVariable Long companyId,
                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                    @RequestParam(required = false) Integer months) {
            List<BankTransaction> transactions = bankReconciliationService.getBankTransactions(
                            companyId, fromDate, toDate, months);

            return ResponseEntity.ok(
                            ApiResponse.successResponse(
                                            HttpStatus.OK.value(),
                                            "Bank transactions fetched successfully",
                                            transactions));
    }
 
}
