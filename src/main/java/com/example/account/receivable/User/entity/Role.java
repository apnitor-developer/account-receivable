package com.example.account.receivable.User.entity;

import java.util.Set;

import com.example.account.receivable.Common.Premission.Permission;
import com.example.account.receivable.Company.Entity.Company;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id")
    )
    @Column(name = "permissions", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions;


    // NULL = global role, NOT NULL = company-specific role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
}


