package com.example.account.receivable.User.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.User.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    boolean existsByNameAndCompanyId(String name, Long companyId);

    List<Role> findByCompanyIdOrCompanyIsNull(Long companyId);
}

