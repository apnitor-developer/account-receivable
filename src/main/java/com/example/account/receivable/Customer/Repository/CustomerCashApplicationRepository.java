package com.example.account.receivable.Customer.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Customer.Entity.CashApplication;

public interface CustomerCashApplicationRepository extends JpaRepository<CashApplication, Long> {
    Optional<CashApplication> findByCustomer_Id(Long customerId);
}