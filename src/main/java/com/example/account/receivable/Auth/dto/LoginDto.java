package com.example.account.receivable.Auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    
    @NotBlank(message = "email cannot be null ")
    private String email;

    @NotBlank(message = "password cannot be ampty")
    private String password;
}
