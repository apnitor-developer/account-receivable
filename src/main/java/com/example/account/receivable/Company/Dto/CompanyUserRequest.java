package com.example.account.receivable.Company.Dto;

import lombok.Data;

@Data
public class CompanyUserRequest {        
    private String name;
    private String email;
    private Long roleId;     
}

