package com.example.account.receivable.Customer.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.account.receivable.Customer.Entity.CustomerStatement;

@Repository
public interface CustomerStatementRepository extends JpaRepository<CustomerStatement, Long> {
    Optional<CustomerStatement> findByCustomer_Id(Long customerId);
}
