package com.example.account.receivable.Invoice.Controller;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.example.account.receivable.Invoice.Dto.OverdueInvoiceResponseDTO;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.CustomerWithPendingAmountResponseDTO;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.InvoiceAgingDto;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.InvoiceStatusBreakdownResponseDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Service.InvoiceService;


@RestController
@RequestMapping("/invoice")
public class InvoiceController {
    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }


    //Add invoice API
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


    // Invoice Approved API
    @PostMapping("/approve/{invoiceId}")
    public ResponseEntity<ApiResponse<Invoice>> approveInvoice(
            @PathVariable Long invoiceId
    ) {
        Invoice invoice = invoiceService.approveInvoice(invoiceId);

        return ResponseEntity.ok(
            ApiResponse.successResponse(200, "Invoice approved", invoice)
        );
    }


    //Get Company Draft Invoice API
    @GetMapping("/company/{companyId}/drafts")
    public ResponseEntity<ApiResponse<Page<Invoice>>> getCompanyDraftInvoices(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Invoice> drafts =
                invoiceService.getDraftInvoicesByCompany(companyId, page, size);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Company draft invoices fetched successfully",
                        drafts
                )
        );
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


    //Get Invoices by the companyId
    @GetMapping("/unpaid/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<Invoice>>> getCompanyOpenPartialInvoices(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer months,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        Page<Invoice> invoices = invoiceService.getOpenAndPartialInvoicesByCompanyId(companyId , page, size , dateFrom, dateTo , months);

        ApiResponse<Page<Invoice>> response = ApiResponse.successResponse(
                200,
                "Company invoices retrieved successfully",
                invoices
        );

        return ResponseEntity.ok(response);
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


    //Get Pending Amount of all the invoices of the Customer
    @GetMapping("/{customerId}/pending-amount-customer")
    public ResponseEntity<ApiResponse<BigDecimal>> getCustomerPendingAmount(@PathVariable Long customerId) {

        BigDecimal pendingAmount = invoiceService.getCustomerPendingAmount(customerId);

        ApiResponse<BigDecimal> response = ApiResponse.successResponse(
            200, 
            "Invoices Retreived Successfully", 
            pendingAmount
        );

        return ResponseEntity.status(200).body(response);
    }


    // API to get company customers with pending amounts (balanceDue > 0)
    @GetMapping("/company/{companyId}/with-pending-amounts")
    public ResponseEntity<ApiResponse<List<CustomerWithPendingAmountResponseDTO>>> getCustomersWithPendingAmountByCompany(@PathVariable Long companyId) {
        List<CustomerWithPendingAmountResponseDTO> customers = invoiceService.getCustomersWithPendingAmountByCompany(companyId);

        ApiResponse<List<CustomerWithPendingAmountResponseDTO>> response = ApiResponse.successResponse(
                200, 
                "Customers with pending amounts retrieved successfully", 
                customers
        );

        return ResponseEntity.ok(response);
    }


    //Get Total Pending Amount of all Company
    @GetMapping("/{companyId}/pending-amount-company")
    public ResponseEntity<ApiResponse<BigDecimal>> getCompanyPendingAmount(
            @PathVariable Long companyId
    ) {
        BigDecimal pendingAmount = invoiceService.getCompanyPendingAmount(companyId);

        ApiResponse<BigDecimal> response = ApiResponse.successResponse(
                200,
                "Company pending amount retrieved successfully",
                pendingAmount
        );

        return ResponseEntity.ok(response);
    }


    // Get all Overdue Invoices of the company and their balance is greater than 0
    @GetMapping("/company/{companyId}/overdue-invoices")
    public ResponseEntity<ApiResponse<List<OverdueInvoiceResponseDTO>>> getOverdueInvoicesByCompany(@PathVariable Long companyId) {

        List<OverdueInvoiceResponseDTO> invoices = invoiceService.getOverdueInvoicesByCompany(companyId);

        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Overdue invoices retrieved successfully",
                invoices
            )
        );
    }


    // GET ALL invoices of a company with optional filters(used in the Invoice Reports)
    // /invoice/company/5?statuses=OPEN,PARTIAL&fromDate=2026-01-01&toDate=2026-01-31&page=0&size=10
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<Invoice>>> getCompanyInvoices(
            @PathVariable Long companyId,
            @RequestParam(required = false) List<InvoiceStatus> statuses,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Invoice> invoices = invoiceService.getCompanyInvoices(companyId, statuses, fromDate, toDate, page, size);

        ApiResponse<Page<Invoice>> response = ApiResponse.successResponse(
                200,
                "Invoices fetched successfully",
                invoices
        );

        return ResponseEntity.ok(response);
    }


    //Calculate Invoice Reports(CURRENT , 0-30 , 30-60 , 60-90 , 90>)
    @GetMapping("/invoice-aging/company/{companyId}")
    public ResponseEntity<ApiResponse<InvoiceAgingDto>> getInvoiceAging(
            @PathVariable Long companyId
    ) {
        InvoiceAgingDto data = invoiceService.getInvoiceAging(companyId);

        return ResponseEntity.ok(
            ApiResponse.successResponse(
                200,
                "Invoice aging report fetched successfully",
                data
            )
        );
    }


    //Calculate Invoice Report(OPEN ,PARTIAL , PAID , WRITTEN_OFF)
    @GetMapping("/status-breakdown/company/{companyId}")
    public ResponseEntity<ApiResponse<InvoiceStatusBreakdownResponseDto>> getInvoiceStatusBreakdown(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "1") int months
    ) {
        InvoiceStatusBreakdownResponseDto data =
                invoiceService.getInvoiceStatusBreakdown(companyId, months);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Invoice status breakdown fetched successfully",
                        data
                )
        );
    }


}
