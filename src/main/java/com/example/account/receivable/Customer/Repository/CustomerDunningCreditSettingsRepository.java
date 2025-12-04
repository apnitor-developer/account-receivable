package com.example.account.receivable.Customer.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;

public interface CustomerDunningCreditSettingsRepository extends JpaRepository<CustomerDunningCreditSettings, Long> {
    Optional<CustomerDunningCreditSettings> findByCustomer_Id(Long customerId);
}
