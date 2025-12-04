package com.example.account.receivable.Customer.Repository;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.receivable.Customer.Entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByDeletedFalse(Pageable pageable);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByIdAndDeletedFalse(Long id);
}
