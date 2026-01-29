package com.example.account.receivable.User.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.Premission.Permission;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.User.dto.RoleSecurityReportDto;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.repository.RoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityReportService {

    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;


    // VIEW A — Role → Object → Permission
    public List<RoleSecurityReportDto> getReportByRole(Long companyId) {

        Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Company not found"
                        ));

        List<Role> roles =
                roleRepository.findByCompanyIdOrCompanyIsNull(companyId);

        return roles.stream()
                .map(role -> new RoleSecurityReportDto(
                        role.getName(),
                        groupPermissionsByObject(role.getPermissions())
                ))
                .toList();
    }


    // VIEW B — Object → Role → Permission
    public Map<String, Map<String, Set<String>>> getReportByObject(Long companyId) {

        Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Company not found"
                        ));

        List<Role> roles =
                roleRepository.findByCompanyIdOrCompanyIsNull(companyId);

        Map<String, Map<String, Set<String>>> result = new TreeMap<>();

        for (Role role : roles) {
            for (Permission permission : role.getPermissions()) {

                String object = resolveObject(permission);
                String action = resolveAction(permission);

                result
                        .computeIfAbsent(object, k -> new TreeMap<>())
                        .computeIfAbsent(role.getName(), k -> new TreeSet<>())
                        .add(action);
            }
        }

        return result;
    }

    // Helper: Group permissions
    private Map<String, Set<String>> groupPermissionsByObject(Set<Permission> permissions) {

        Map<String, Set<String>> map = new TreeMap<>();

        for (Permission permission : permissions) {
            String object = resolveObject(permission);
            String action = resolveAction(permission);

            map
                    .computeIfAbsent(object, k -> new TreeSet<>())
                    .add(action);
        }
        return map;
    }

    // ==============================
    // Permission → Object
    // ==============================
    private String resolveObject(Permission permission) {

        String name = permission.name();

        if (name.contains("DASHBOARD")) return "Dashboard";
        if (name.contains("CUSTOMER")) return "Customers";
        if (name.contains("INVOICE")) return "Invoices";
        if (name.contains("PAYMENT")) return "Payments";
        if (name.contains("AGING") || name.contains("REPORT")) return "Reports";
        if (name.contains("COLLECTION")) return "Collections";
        if (name.contains("REMINDER")) return "Reminders";
        if (name.contains("PROMISE")) return "Promise To Pay";
        if (name.contains("DISPUTE")) return "Disputes";
        if (name.contains("MEMO")) return "Credit Memos";
        if (name.contains("WRITE_OFF")) return "Write Off";
        if (name.contains("COMPANY")) return "Company";
        if (name.contains("USER")) return "Users";
        if (name.contains("ROLE")) return "Roles";
        if (name.contains("SECURITY")) return "Security report";
        if (name.contains("AR")) return "AR";
        if (name.contains("GL")) return "GL";
        


        return "Other";
    }

    // ==============================
    // Permission → Action
    // ==============================
    private String resolveAction(Permission permission) {

        String name = permission.name();

        if (name.startsWith("VIEW")) return "VIEW";
        if (name.startsWith("CREATE")) return "CREATE";
        if (name.startsWith("EDIT") || name.startsWith("UPDATE")) return "EDIT";
        if (name.startsWith("DELETE")) return "DELETE";
        if (name.startsWith("APPROVE")) return "APPROVE";
        if (name.startsWith("APPLY")) return "APPLY";
        if (name.startsWith("SEND")) return "SEND";

        return name;
    }
}
