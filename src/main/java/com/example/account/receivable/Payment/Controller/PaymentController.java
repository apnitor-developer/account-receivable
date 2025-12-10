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
import org.springframework.data.domain.Page;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
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
}
