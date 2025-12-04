package com.example.account.receivable.Customer.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.account.receivable.Customer.Entity.CustomerEFT;

@Repository
public interface CustomerEftRepository extends JpaRepository<CustomerEFT, Long> {
    Optional<CustomerEFT> findByCustomer_Id(Long customerId);
}
