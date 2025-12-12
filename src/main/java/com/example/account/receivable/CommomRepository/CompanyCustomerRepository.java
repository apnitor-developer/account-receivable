package com.example.account.receivable.CommomRepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.account.receivable.CommonEntity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.Customer;

public interface CompanyCustomerRepository extends JpaRepository<CompanyCustomers , Long>{
        @Query("""
        SELECT cc.customer
        FROM CompanyCustomers cc
        WHERE cc.company.id = :companyId
          AND cc.customer.deleted = false
    """)
    List<Customer> findCustomersByCompanyId(Long companyId);
}
