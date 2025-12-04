package com.example.sqlserver.Dto;

import lombok.Data;

@Data
public class CompanyUserRequest {
    private Long id;         
    private String name;
    private String email;
    private String status;   
    private Long roleId;     
}

