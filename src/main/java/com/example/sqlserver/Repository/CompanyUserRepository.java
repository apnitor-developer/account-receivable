package com.example.sqlserver.Repository;

import com.example.sqlserver.Entity.CompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyUserRepository extends JpaRepository<CompanyUser, Long> {

    // all users for a company
    List<CompanyUser> findByCompany_Id(Long companyId);

    // single user by company + email (for duplicate check / upsert)
    Optional<CompanyUser> findByCompany_IdAndEmail(Long companyId, String email);
}


