package com.example.account.receivable.Payment.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Payment.Entity.PaymentApplication;

public interface PaymentApplicationRepository extends JpaRepository<PaymentApplication, Long> {}
