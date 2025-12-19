package com.example.account.receivable.User.seed;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.account.receivable.Common.Premission.Permission;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.repository.RoleRepository;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RoleSeeder {

    private final RoleRepository roleRepository;

    @PostConstruct
    public void seedRoles() {

        if (!roleRepository.existsByName("Super Admin")) {
            roleRepository.save(
                Role.builder()
                    .name("Super Admin")
                    .description("Every access")
                    .permissions(Set.of(Permission.values())) // ALL permissions
                    .build()
            );
        }

        if (!roleRepository.existsByName("Owner")) {
            roleRepository.save(
                Role.builder()
                    .name("Owner")
                    .description("Full Company access")
                    .permissions(Set.of(Permission.values()))
                    .build()
            );
        }

        System.out.println("✅ Roles & permissions seeded.");
    }
}
