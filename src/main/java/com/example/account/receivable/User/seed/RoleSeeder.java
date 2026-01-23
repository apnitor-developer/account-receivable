package com.example.account.receivable.User.seed;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.account.receivable.Common.Premission.Permission;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.repository.RoleRepository;

import java.util.EnumSet;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void seedRoles() {

        // =========================
        // ADMIN (System use only)
        // =========================
        createOrUpdateRole(
            "Admin",
            "System administrator with full access",
            EnumSet.allOf(Permission.class)
        );

        // =========================
        // AR SPECIALIST
        // =========================
        createOrUpdateRole(
            "Ar_Specialist",
            "Processes invoices and applies payments",
            EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.VIEW_CUSTOMERS,
                Permission.VIEW_INVOICES,
                Permission.CREATE_INVOICE,
                Permission.EDIT_INVOICE,
                Permission.VIEW_PAYMENTS,
                Permission.APPLY_PAYMENT,
                Permission.VIEW_AGING_REPORTS
            )
        );

        // =========================
        // CASH APPLICATION
        // =========================
        createOrUpdateRole(
            "Cash_Application",
            "Applies customer payments",
            EnumSet.of(
                Permission.VIEW_CUSTOMERS,
                Permission.VIEW_INVOICES,
                Permission.VIEW_PAYMENTS,
                Permission.APPLY_PAYMENT
            )
        );

        // =========================
        // CUSTOMER CREATION
        // =========================
        createOrUpdateRole(
            "Customer_Creation",
            "Creates and manages customers only",
            EnumSet.of(
                Permission.VIEW_CUSTOMERS,
                Permission.CREATE_CUSTOMER,
                Permission.EDIT_CUSTOMER
            )
        );

        // =========================
        // AR MANAGER
        // =========================
        createOrUpdateRole(
            "Ar_Manager",
            "Manages AR operations and approvals",
            EnumSet.of(
                Permission.VIEW_DASHBOARD,
                Permission.VIEW_CUSTOMERS,
                Permission.VIEW_INVOICES,
                Permission.VIEW_PAYMENTS,
                Permission.VIEW_AGING_REPORTS,
                Permission.VIEW_COLLECTIONS,
                Permission.APPROVE_MEMOS,
                Permission.APPROVE_WRITE_OFF,
                Permission.VIEW_WRITE_OFF
            )
        );

        System.out.println("✅ Standard AR roles seeded successfully.");
    }

    private void createOrUpdateRole(String name, String description, EnumSet<Permission> permissions) {
        Role role = roleRepository.findByName(name)
            .orElseGet(() -> Role.builder()
                .name(name)
                .description(description)
                .company(null)
                .build()
            );

        role.setPermissions(permissions);
        roleRepository.save(role);
    }
}

