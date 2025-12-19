package com.example.account.receivable.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.User.entity.Users;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u JOIN u.userCompanies uc WHERE uc.company.id = :companyId")
    List<Users> findByCompany_Id(@Param("companyId") Long companyId);

    // Get a single user by company and email (for duplicate check / upsert)
    @Query("SELECT u FROM Users u JOIN u.userCompanies uc WHERE uc.company.id = :companyId AND u.email = :email")
    Optional<Users> findByCompany_IdAndEmail(@Param("companyId") Long companyId, @Param("email") String email);

    Optional<Users> findByEmail(String email);
    Optional<Users> findByEmailAndDeletedFalse(String email);
}


