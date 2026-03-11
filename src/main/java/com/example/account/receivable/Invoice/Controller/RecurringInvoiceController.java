package com.example.account.receivable.Invoice.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.Invoice.Dto.RecurringInvoiceTemplateDto;
import com.example.account.receivable.Invoice.Entity.RecurringInvoiceTemplate;
import com.example.account.receivable.Invoice.Service.RecurringInvoiceService;


import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/recurring-invoices")
@RequiredArgsConstructor
public class RecurringInvoiceController {

    private final RecurringInvoiceService recurringInvoiceService;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringInvoiceTemplate>> createTemplate( @RequestBody RecurringInvoiceTemplateDto dto) {
        RecurringInvoiceTemplate invoice = recurringInvoiceService.createTemplate(dto); 
        ApiResponse<RecurringInvoiceTemplate> response = ApiResponse.successResponse(
            201,
            "Recruting invoice created successfully", 
            invoice
        );
        return ResponseEntity.status(201).body(response);


    }
}
