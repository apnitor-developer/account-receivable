package com.example.account.receivable.Customer.Dto.CustomerDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerDTO {
    
    private String customerName;
    private String email;
    private String customerType;

    @NotNull
    private String phoneNumber;

    private String faceBook;
    private String linkedin;
    private String twitter;
}
