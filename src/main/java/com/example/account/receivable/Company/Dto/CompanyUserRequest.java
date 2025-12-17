package com.example.account.receivable.Company.Dto;

import lombok.Data;

@Data
public class CompanyUserRequest {        
    private String firstName;
    private String lastName;
    private String email;
    private Long roleIds;     
}

