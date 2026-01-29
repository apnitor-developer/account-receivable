package com.example.account.receivable.User.dto;

import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleSecurityReportDto {

    private String role;

    // Key = Object (Invoices, Customers, etc.)
    // Value = Actions (VIEW, CREATE, EDIT...)
    private Map<String, Set<String>> objects;
}

