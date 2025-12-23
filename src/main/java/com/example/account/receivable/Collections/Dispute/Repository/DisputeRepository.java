package com.example.account.receivable.Collections.Dispute.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.Collections.Dispute.Entity.Dispute;

public interface DisputeRepository extends JpaRepository<Dispute , Long>{
    Optional<Dispute> findTopByDisputeIdStartingWithOrderByDisputeIdDesc(String prefix);

    boolean existsByDisputeId(String disputeId);


    //Get Company Disputes
    @Query("""
        SELECT d
        FROM Dispute d
        JOIN d.invoice i
        JOIN i.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
    """)
    List<Dispute> findAllByCompanyId(@Param("companyId") Long companyId);

    
}
