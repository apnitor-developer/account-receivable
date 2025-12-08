package com.example.account.receivable.CommomRepository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.CommonEntity.CompanyCustomers;

public interface CompanyCustomerRepository extends JpaRepository<CompanyCustomers , Long>{
}
