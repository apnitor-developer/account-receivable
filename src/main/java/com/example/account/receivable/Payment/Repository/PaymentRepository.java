package com.example.account.receivable.Payment.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Payment.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
}