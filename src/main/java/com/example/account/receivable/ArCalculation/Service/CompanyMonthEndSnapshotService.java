package com.example.account.receivable.ArCalculation.Service;

import com.example.account.receivable.ArCalculation.Entity.CompanyMonthEndBalance;
import com.example.account.receivable.ArCalculation.Repository.CompanyMonthEndBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class CompanyMonthEndSnapshotService {

    private final CompanyBalanceCalculator companyBalanceCalculator; // your existing one
    private final CompanyMonthEndBalanceRepository snapshotRepo;

    @Transactional
    public CompanyMonthEndBalance calculateAndSave(Long companyId, YearMonth month) {

        String ym = month.toString(); // "YYYY-MM"
        var asOfDate = month.atEndOfMonth();

        var existingOpt = snapshotRepo.findByCompanyIdAndYearMonth(companyId, ym);

        CompanyMonthEndBalance entity = existingOpt.orElseGet(() ->
                CompanyMonthEndBalance.builder()
                        .companyId(companyId)
                        .yearMonth(ym)
                        .asOfDate(asOfDate)
                        .locked(true) // set true if you want it fixed
                        .build()
        );

        // If locked and exists → don’t change it
        if (entity.getId() != null && Boolean.TRUE.equals(entity.getLocked())) {
            return entity;
        }

        var dto = companyBalanceCalculator.calculate(companyId, month);

        entity.setBalance(dto.getMonthEndBalance());
        entity.setCalculatedAt(LocalDateTime.now());
        entity.setAsOfDate(asOfDate);

        return snapshotRepo.save(entity);
    }


    public boolean exists(Long companyId, YearMonth month) {
    return snapshotRepo.findByCompanyIdAndYearMonth(
            companyId,
            month.toString()
    ).isPresent();
}
}
