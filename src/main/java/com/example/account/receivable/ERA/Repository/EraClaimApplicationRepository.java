package com.example.account.receivable.ERA.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.account.receivable.ERA.Entity.EraClaimApplication;

public interface EraClaimApplicationRepository extends JpaRepository<EraClaimApplication, Long>{
    
}
