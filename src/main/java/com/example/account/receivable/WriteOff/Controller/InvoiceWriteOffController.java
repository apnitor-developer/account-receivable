package com.example.account.receivable.WriteOff.Controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.WriteOff.Dto.CompanyWriteOffResponse;
import com.example.account.receivable.WriteOff.Dto.CreateWriteOffRequest;
import com.example.account.receivable.WriteOff.Service.InvoiceWriteOffService;
import com.example.account.receivable.WriteOff.Entity.InvoiceWriteOff;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/write-offs")
@RequiredArgsConstructor
public class InvoiceWriteOffController {

    private final InvoiceWriteOffService writeOffService;

    // Create WriteOff Invoice
    @PostMapping("/company/{companyId}/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<InvoiceWriteOff>> createWriteOff(
            @PathVariable Long companyId,
            @PathVariable Long invoiceId,
            @RequestBody CreateWriteOffRequest req
    ) {
        InvoiceWriteOff writeOff =
                writeOffService.writeOffInvoice(companyId , invoiceId, req);

        return ResponseEntity.status(201)
                .body(ApiResponse.successResponse(
                        201,
                        "Invoice written off successfully",
                        writeOff
                ));
    }


    // Get Write-Off Invoice of the company
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<CompanyWriteOffResponse>>> getCompanyWriteOffs(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
        ) {
            Page<CompanyWriteOffResponse> response =
                    writeOffService.getCompanyWriteOffs(companyId, page, size);

            return ResponseEntity.ok(
                    ApiResponse.successResponse(
                            200,
                            "Company write-offs retrieved successfully",
                            response
                    )
            );
        }
}

