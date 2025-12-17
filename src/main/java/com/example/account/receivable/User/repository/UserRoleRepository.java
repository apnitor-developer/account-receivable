package com.example.account.receivable.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.receivable.User.entity.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    
}
