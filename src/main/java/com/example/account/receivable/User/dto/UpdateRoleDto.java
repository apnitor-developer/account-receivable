package com.example.account.receivable.User.dto;

import java.util.Set;

import com.example.account.receivable.Common.Premission.Permission;

import lombok.Data;

@Data
public class UpdateRoleDto {

    // Basic info
    private String name;
    private String description;

    // Permission operations
    private Set<Permission> addPermissions;
    private Set<Permission> removePermissions;
}

