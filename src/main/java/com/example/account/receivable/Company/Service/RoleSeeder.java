package com.example.account.receivable.Company.Service;

import com.example.account.receivable.Company.Entity.Role;
import com.example.account.receivable.Company.Repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seedRole("Admin",   "Full access to all company settings and users");
        seedRole("Manager", "Can manage team, companies, and approvals");
        seedRole("User",    "Standard user with limited permissions");
        seedRole("Viewer",  "Read-only access");
    }

    private void seedRole(String name, String description) {
        if (!roleRepository.existsByName(name)) {
            roleRepository.save(
                    Role.builder()
                            .name(name)
                            .description(description)
                            .build()
            );
        }
    }
}

