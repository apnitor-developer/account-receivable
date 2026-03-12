package com.example.account.receivable.Customer.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.Customer.Entity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.Customer;

public interface CompanyCustomerRepository extends JpaRepository<CompanyCustomers , Long>{
    //     @Query("""
    //     SELECT cc.customer
    //     FROM CompanyCustomers cc
    //     WHERE cc.company.id = :companyId
    //       AND cc.customer.deleted = false
    // """)
    // List<Customer> findCustomersByCompanyId(Long companyId);

    Optional<CompanyCustomers> findFirstByCustomer_Id(Long customerId);


    //Get the non-deleted customers by the companyId
    @Query("select cc.customer from CompanyCustomers cc " +
          "where cc.company.id = :companyId and cc.customer.deleted = false")
    Page<Customer> findActiveCustomersByCompanyId(@Param("companyId") Long companyId, Pageable pageable);

    boolean existsByCompany_IdAndCustomer_Id(Long companyId, Long customerId);


    
    @Query("""
        select cc.customer
        from CompanyCustomers cc
        where cc.company.id = :companyId
          and cc.customer.email = :email
          and cc.customer.deleted = false
    """)
    Optional<Customer> findCustomerByCompanyIdAndEmail(
            @Param("companyId") Long companyId,
            @Param("email") String email
    );
}
