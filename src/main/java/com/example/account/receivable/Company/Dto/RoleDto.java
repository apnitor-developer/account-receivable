package com.example.account.receivable.Company.Dto;

import java.util.Set;

import com.example.account.receivable.Common.Premission.Permission;

import lombok.Data;

@Data
public class RoleDto {
    private String name;
    private String description;
    private Set<Permission> permissions;
}
