package com.example.account.receivable.ArCalculation.Service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.example.account.receivable.ArCalculation.Entity.CompanyYearEndClose;
import com.example.account.receivable.ArCalculation.Repository.CompanyMonthEndBalanceRepository;
import com.example.account.receivable.ArCalculation.Repository.CompanyYearCloseRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class YearEndCloseService {

    private final CompanyMonthEndBalanceRepository snapshotRepo;
    private final CompanyYearCloseRepository yearRepo;

    @Transactional
    public void closeYear(Long companyId, int year) {

        String december = year + "-12";

        var snapshot =
                snapshotRepo.findByCompanyIdAndYearMonth(companyId, december)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "December snapshot missing. Run month-end close first."
                                ));

        CompanyYearEndClose close =
                CompanyYearEndClose.builder()
                        .companyId(companyId)
                        .year(year)
                        .closed(true)
                        .closedAt(LocalDateTime.now())
                        .build();

        yearRepo.save(close);
    }
}
