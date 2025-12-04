package com.example.account.receivable.Company.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}

