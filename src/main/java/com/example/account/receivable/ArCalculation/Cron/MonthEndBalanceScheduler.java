package com.example.account.receivable.ArCalculation.Cron;

import com.example.account.receivable.ArCalculation.Service.CompanyMonthEndSnapshotService;
import com.example.account.receivable.Company.Repository.CompanyRepository; // adjust to your actual path
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MonthEndBalanceScheduler {

    private final CompanyMonthEndSnapshotService snapshotService;
    private final CompanyRepository companyRepository;

    // Runs on 1st day of every month at 00:10 (India time)
    @Scheduled(cron = "0 10 0 1 * *", zone = "Asia/Kolkata")
    public void runMonthlySnapshot() {

        YearMonth previousMonth = YearMonth.now().minusMonths(1);

        List<Long> companyIds = companyRepository.findActiveCompanyIds(); // create this method

        for (Long companyId : companyIds) {
            snapshotService.calculateAndSave(companyId, previousMonth);
        }
    }
}
