package com.example.account.receivable.Company.Entity;

import java.util.Set;

import com.example.account.receivable.Common.Premission.Permission;

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
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions;
}

