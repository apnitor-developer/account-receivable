package com.example.account.receivable.Customer.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.receivable.Customer.Entity.CustomerVAT;

public interface CustomerVatRepository extends JpaRepository<CustomerVAT, Long> {
    Optional<CustomerVAT> findByCustomer_Id(Long customerId);
}
