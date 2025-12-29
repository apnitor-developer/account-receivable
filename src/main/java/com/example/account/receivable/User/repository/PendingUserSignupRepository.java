package com.example.account.receivable.User.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.User.entity.PendingUserSignup;

public interface PendingUserSignupRepository extends JpaRepository<PendingUserSignup, Long> {

    Optional<PendingUserSignup> findByEmail(String email);
}

