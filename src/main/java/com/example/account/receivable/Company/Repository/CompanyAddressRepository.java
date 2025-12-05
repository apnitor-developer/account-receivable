package com.example.account.receivable.Company.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Company.Entity.CompanyAddress;

public interface CompanyAddressRepository extends JpaRepository<CompanyAddress, Long> {
    Optional<CompanyAddress> findByCompany_Id(Long id);
}
