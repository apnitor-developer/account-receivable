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

        Role admin = roleRepository.findByName("Admin")
            .orElseGet(() -> Role.builder()
                .name("Admin")
                .description("Every access")
                .company(null)
                .build()
            );

        admin.setPermissions(EnumSet.allOf(Permission.class));

        roleRepository.save(admin);

        System.out.println("✅ Admin role synced with all permissions.");
    }
}
