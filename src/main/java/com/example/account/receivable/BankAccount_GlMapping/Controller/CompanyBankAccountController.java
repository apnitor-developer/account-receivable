package com.example.account.receivable.BankAccount_GlMapping.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingDetailDto;
import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountListDto;
import com.example.account.receivable.BankAccount_GlMapping.Service.CompanyBankAccountService;
import com.example.account.receivable.Common.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyBankAccountController {

    private final CompanyBankAccountService service;

    // Get Company Bank Accounts
    @GetMapping("/{companyId}/bank-accounts")
    public ResponseEntity<ApiResponse<List<BankAccountListDto>>> getAccounts(
            @PathVariable Long companyId
    ) {
        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Company bank accounts fetched",
                service.getCompanyBankAccounts(companyId)
            )
        );
    }


    // Get Bank Account Mapping Details
    @GetMapping("/{bankAccountId}/gl-mapping")
    public ResponseEntity<ApiResponse<BankAccountGlMappingDetailDto>> getMapping(
            @PathVariable Long bankAccountId
    ) {
        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Bank account GL mapping fetched",
                service.getMappingDetail(bankAccountId)
            )
        );
    }
}

