package com.example.account.receivable.Payment.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Dto.ResponseDTO.MonthlyPaymentDto;
import com.example.account.receivable.Payment.Dto.ResponseDTO.PaymentReportDto;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Service.PaymentService;

@RestController
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    //Apply Payment
    @PostMapping("/apply/{customerId}")
    public ResponseEntity<ApiResponse<Payment>> applyPayment(
        @PathVariable("customerId") Long customerId,
        @RequestBody ReceivePaymentRequest dto
    ){
        Payment payment = paymentService.applyPayment(customerId , dto);

        ApiResponse<Payment> response = ApiResponse.successResponse(201, "Payment applied successfully", payment);
        return ResponseEntity.status(201).body(response);
    }


    //Get All Payments
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<Payment>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Payment> payment = paymentService.getAllPayments(page , size);
        ApiResponse<Page<Payment>> response = ApiResponse.successResponse(
            201, 
            "Payments Retreived successfully", 
            payment
        );
        return ResponseEntity.status(200).body(response);
    }


    //Get Payments by the companyId
    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<Page<Payment>>> getPaymentsByCompanyId(
            @PathVariable("companyId") Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Page<Payment> payment = paymentService.getPaymentsByCompanyId(companyId , page , size);
        ApiResponse<Page<Payment>> response = ApiResponse.successResponse(
            201, 
            "Payments Retreived successfully", 
            payment
        );
        return ResponseEntity.status(200).body(response);
    }


    
    // Get All Payments with date filter(used in the Paymnet Report)
    @GetMapping("/company/{companyId}/filter")
    public ResponseEntity<ApiResponse<Page<Payment>>> getPaymentsByCompanyId(
            @PathVariable("companyId") Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(required = false) Integer months
    ) {
        Page<Payment> payments = paymentService.getPaymentsByCompanyId(companyId, page, size, fromDate, toDate , months);

        ApiResponse<Page<Payment>> response = ApiResponse.successResponse(
                200,
                "Payments retrieved successfully",
                payments
        );

        return ResponseEntity.ok(response);
    }



    // API used to calculate the payment report(BANK_TRANSFER , CASH , UPI etc)
    @GetMapping("/report/company/{companyId}")
    public ResponseEntity<ApiResponse<PaymentReportDto>> getPaymentReport(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "1") int months
    ) {
        PaymentReportDto data =
                paymentService.getPaymentReport(companyId, months);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Payment report fetched successfully",
                        data
                )
        );
    }



    //API used to show the paymnet per months
    @GetMapping("/monthly/company/{companyId}")
    public ResponseEntity<ApiResponse<List<MonthlyPaymentDto>>> getMonthlyPayments(
            @PathVariable Long companyId,
            @RequestParam int year
    ) {
        List<MonthlyPaymentDto> data =
                paymentService.getMonthlyPaymentsByYear(companyId, year);

        return ResponseEntity.ok(
                ApiResponse.successResponse(
                        200,
                        "Monthly payment report fetched successfully",
                        data
                )
        );
    }


}
