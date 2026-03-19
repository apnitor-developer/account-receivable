package com.example.account.receivable.ArCalculation.Repository;

import com.example.account.receivable.ArCalculation.Entity.CompanyMonthEndBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CompanyMonthEndBalanceRepository extends JpaRepository<CompanyMonthEndBalance, Long> {

    Optional<CompanyMonthEndBalance> findByCompanyIdAndYearMonth(Long companyId, String yearMonth);

    List<CompanyMonthEndBalance> findByCompanyIdAndYearMonthBetweenOrderByYearMonthAsc(
            Long companyId,
            String startYearMonth,
            String endYearMonth
    );

    boolean existsByCompanyIdAndYearMonthAndLockedTrue(Long companyId, String yearMonth);

    @Query("""
    SELECT c 
    FROM CompanyMonthEndBalance c
    WHERE c.companyId = :companyId
      AND SUBSTRING(c.yearMonth, 1, 4) = :year
    ORDER BY c.yearMonth
        """)
    List<CompanyMonthEndBalance> findByCompanyIdAndYear(
            Long companyId,
            String year
    );
}
