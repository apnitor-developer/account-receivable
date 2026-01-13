package com.example.account.receivable.BankReconciliation.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.account.receivable.BankReconciliation.Service.BankReconciliationService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-reconciliation")
@RequiredArgsConstructor
public class BankReconciliationController {

    private final BankReconciliationService bankReconciliationService;

    // Upload BAI file
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> uploadBaiFile(
            @RequestParam("file") MultipartFile file) {

        bankReconciliationService.processBaiFile(file);

        ApiResponse<String> response =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "BAI file processed successfully",
                        "SUCCESS"
                );

        return ResponseEntity.ok(response);
    }
}
