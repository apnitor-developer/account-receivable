package com.example.sqlserver.Dto;


import lombok.Data;
import java.util.List;

@Data
public class ManageUsersRequest {
    private List<CompanyUserRequest> users;
}

