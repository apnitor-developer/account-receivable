package com.example.account.receivable.Exception;

import com.example.account.receivable.Common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles ResponseStatusException (like your "Customer not found")
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {

        int statusCode = ex.getStatusCode().value();
        String message = ex.getReason() != null ? ex.getReason() : "Error";

        ApiResponse<Void> body = ApiResponse.errorResponse(statusCode, message);

        return ResponseEntity.status(statusCode).body(body);
    }

    // Fallback for any other unhandled exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {

        ApiResponse<Void> body = ApiResponse.errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


    @ExceptionHandler(DuplicateCustomerException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateCustomer(DuplicateCustomerException ex) {

        ApiResponse<Void> response =
                ApiResponse.errorResponse(409, ex.getMessage()); // 409 Conflict

        return ResponseEntity.status(409).body(response);
    }
}
