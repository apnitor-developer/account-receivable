package com.example.account.receivable.Company.Dto;

import java.util.List;

import lombok.Data;

@Data
public class CompanyUserRequest {        
    private String firstName;
    private String lastName;
    private String email;
    private List<Long> roleIds; 
}

