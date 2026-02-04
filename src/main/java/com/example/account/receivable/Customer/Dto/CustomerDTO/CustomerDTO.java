package com.example.account.receivable.Customer.Dto.CustomerDTO;

import com.example.account.receivable.Customer.Enum.CustomerTypeEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CustomerDTO {
    
    private String customerName;
    private String email;
    private CustomerTypeEnum customerType;

    @NotNull
    private String phoneNumber;

    private String faceBook;
    private String linkedin;
    private String twitter;
}
