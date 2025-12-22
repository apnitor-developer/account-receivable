package com.example.account.receivable.Customer.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.Customer.Entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Page<Customer> findByDeletedFalse(Pageable pageable);
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByIdAndDeletedFalse(Long id);

    //Count Total Customers
    long countByDeletedFalse();

    //Used in the Dashboard
    @Query("""
        SELECT COUNT(c)
        FROM Customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
        AND c.deleted = false
    """)
    long countByCompanyAndDeletedFalse(@Param("companyId") Long companyId);

    //Fetch customers for a company with pending balance 
    @Query("""
        SELECT DISTINCT c
        FROM Customer c
        JOIN c.companyCompanies cc
        JOIN Invoice i ON i.customer = c
        WHERE cc.company.id = :companyId
          AND c.deleted = false
          AND i.deleted = false
          AND i.balanceDue > 0
    """)
    List<Customer> findCustomersWithPendingByCompanyId(@Param("companyId") Long companyId);

}
