package com.example.account.receivable.ArCalculation.Repository;

import com.example.account.receivable.ArCalculation.Entity.CompanyMonthEndBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyMonthEndBalanceRepository extends JpaRepository<CompanyMonthEndBalance, Long> {

    Optional<CompanyMonthEndBalance> findByCompanyIdAndYearMonth(Long companyId, String yearMonth);

    List<CompanyMonthEndBalance> findByCompanyIdAndYearMonthBetweenOrderByYearMonthAsc(
            Long companyId,
            String startYearMonth,
            String endYearMonth
    );
}
