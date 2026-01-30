package com.example.account.receivable.BankAccount_GlMapping.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingRequest;
import com.example.account.receivable.BankAccount_GlMapping.Entity.BankAccountGlMapping;
import com.example.account.receivable.BankAccount_GlMapping.Service.BankAccountGlMappingService;
import com.example.account.receivable.Common.ApiResponse;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-gl-mappings")
@RequiredArgsConstructor
public class BankAccountGlMappingController {

    private final BankAccountGlMappingService service;

    // Create mapping 
    @PostMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<BankAccountGlMapping>> create(
            @PathVariable Long companyId,
            @RequestBody @Valid BankAccountGlMappingRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Bank account GL mapping created",
                service.createMapping(companyId, request)
            )
        );
    }

    // Update mapping
    @PutMapping("/company/{companyId}/{mappingId}")
    public ResponseEntity<ApiResponse<BankAccountGlMapping>> update(
            @PathVariable Long companyId,
            @PathVariable Long mappingId,
            @RequestBody BankAccountGlMappingRequest request
    ) {
        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Bank account GL mapping updated",
                service.updateMapping(companyId, mappingId, request)
            )
        );
    }
}


