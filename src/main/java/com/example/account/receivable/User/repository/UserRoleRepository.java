package com.example.account.receivable.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.entity.UserRole;
import com.example.account.receivable.User.entity.Users;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    boolean existsByUserAndRole(Users user, Role role);
}
