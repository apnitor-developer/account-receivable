package com.example.account.receivable.Company.Dto;


import lombok.Data;
import java.util.List;

@Data
public class ManageUsersRequest {
    private List<CompanyUserRequest> users;
}

