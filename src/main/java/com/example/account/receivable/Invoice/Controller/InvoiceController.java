package com.example.account.receivable.Invoice.Controller;


import java.util.List;

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
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Service.InvoiceService;


@RestController
@RequestMapping("/invoice")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }


    //Add invoice
    @PostMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Invoice>> createInvoice(
        @PathVariable("customerId") Long customerId,
        @RequestBody InvoiceDto invoiceDto){
        Invoice invoice = invoiceService.createInvoice(customerId ,invoiceDto);
        ApiResponse<Invoice> response = ApiResponse.successResponse(
            201, 
            "User invoice created successfully", 
            invoice
        );
        return ResponseEntity.status(201).body(response);
    }

    //Send Invoice
    @PostMapping("/send/{invoiceId}")
    public ResponseEntity<ApiResponse<String>> sendInvoice(
            @PathVariable Long invoiceId
    ) {
        invoiceService.sendInvoiceEmail(invoiceId);

        ApiResponse<String> res = ApiResponse.successResponse(
                200,
                "Invoice sent successfully",
                "sent"
        );

        return ResponseEntity.ok(res);
    }


    //Get OPEN and PARTIAL invoices
    @GetMapping("/unpaid/{customerId}")
    public ResponseEntity<ApiResponse<List<Invoice>>> getOpenInvoices(
        @PathVariable("customerId") Long customerId
    ){
        List<Invoice> invoices = invoiceService.getOpenInvoices(customerId);

        ApiResponse<List<Invoice>> response = ApiResponse.successResponse(
            200, 
            "Customer unpaid Invoices retrived successfully",
            invoices
        );
        return ResponseEntity.status(200).body(response);
    }



    //Get All Invoices
    @GetMapping()
    public ResponseEntity<ApiResponse <Page<Invoice>>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Invoice> invoice = invoiceService.getAllInvoices(page , size);
        ApiResponse<Page<Invoice>> response = ApiResponse.successResponse(
            200,
            "Invoices Retreived Successfully", 
            invoice
        );
        return ResponseEntity.status(200).body(response);
    }

    //Get single customer Invoice
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse <List<Invoice>>> getSingleCustomerInvoice(
        @PathVariable("customerId") Long customerId
    ){
        List<Invoice> invoice = invoiceService.getSingleCustomerInvoice(customerId);
        ApiResponse<List<Invoice>> response = ApiResponse.successResponse(
            200,
            "Invoices Retreived Successfully", 
            invoice
        );
        return ResponseEntity.status(200).body(response);
    }

    
    //Invoice By Id
    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse <Invoice>> getInvoice(
        @PathVariable("invoiceId") Long invoiceId
    ){
        Invoice invoice = invoiceService.getInvoice(invoiceId);
        ApiResponse<Invoice> response = ApiResponse.successResponse(
            200, 
            "Invoices Retreived Successfully", 
            invoice
        );
        return ResponseEntity.status(200).body(response);
    }

}
