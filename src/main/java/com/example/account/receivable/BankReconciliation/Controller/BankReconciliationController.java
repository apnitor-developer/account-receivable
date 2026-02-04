package com.example.account.receivable.BankReconciliation.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.account.receivable.BankReconciliation.Service.BankReconciliationService;
import com.example.account.receivable.Common.ApiResponse;

import jakarta.websocket.server.PathParam;
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
}
