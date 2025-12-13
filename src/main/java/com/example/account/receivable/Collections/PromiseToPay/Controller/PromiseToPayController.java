package com.example.account.receivable.Collections.PromiseToPay.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Collections.PromiseToPay.DTO.RequestDTO.PromiseToPayRequest;
import com.example.account.receivable.Collections.PromiseToPay.DTO.ResponseDTO.PromiseToPayResponse;
import com.example.account.receivable.Collections.PromiseToPay.Service.PromiseToPayService;
import com.example.account.receivable.Common.ApiResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/collections/promise")
@RequiredArgsConstructor
public class PromiseToPayController {

    private final PromiseToPayService promiseToPayService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromiseToPayResponse>> createPromise(
            @RequestBody PromiseToPayRequest request
    ) {
        PromiseToPayResponse response = promiseToPayService.createPromise(request);

        ApiResponse<PromiseToPayResponse> body = ApiResponse.successResponse(
            201, 
            "Promise to pay created successfully", 
            response
        );
        return ResponseEntity.status(201).body(body);
    }


    //Get All Promise to Pay for all the customers
    @GetMapping
    public ResponseEntity<ApiResponse<List<PromiseToPayResponse>>> getAllPromiseToPay(){
        List<PromiseToPayResponse> response = promiseToPayService.getAllPromiseToPay();

        ApiResponse<List<PromiseToPayResponse>> body = ApiResponse.successResponse(
            200, 
            "Promise to pay retreived successfully", 
            response
        );
        return ResponseEntity.status(200).body(body);
    }


    //Get Promise to pay for a particualr customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<PromiseToPayResponse>>> 
    getPromiseToPayByCustomer(@PathVariable Long customerId) {

        List<PromiseToPayResponse> response =
                promiseToPayService.getPromiseToPayByCustomer(customerId);

        ApiResponse<List<PromiseToPayResponse>> body =
                ApiResponse.successResponse(
                        200,
                        "Customer promise to pay retrieved successfully",
                        response
                );

        return ResponseEntity.status(200).body(body);
    }
}

