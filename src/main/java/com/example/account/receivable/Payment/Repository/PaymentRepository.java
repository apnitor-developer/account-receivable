package com.example.account.receivable.Payment.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.Payment.Entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    //Total Payment Received
    @Query("""
    SELECT COALESCE(SUM(p.paymentAmount), 0)
    FROM Payment p
    """)
    BigDecimal getTotalPayments();

    
    //Total Today payment Received
    @Query("""
        SELECT COALESCE(SUM(p.paymentAmount), 0)
        FROM Payment p
        WHERE p.paymentDate = :today
    """)
    BigDecimal getTodayPayments(@Param("today") LocalDate today);

}