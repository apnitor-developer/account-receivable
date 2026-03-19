package com.example.account.receivable.ArCalculation.Cron;

import com.example.account.receivable.ArCalculation.Service.CompanyMonthEndSnapshotService;
import com.example.account.receivable.Company.Repository.CompanyRepository; // adjust to your actual path
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthEndBalanceScheduler {

    private final CompanyMonthEndSnapshotService snapshotService;
    private final CompanyRepository companyRepository;

    // Runs on 1st day of every month at 00:10 (India time)
    @Scheduled(cron = "0 1 0 1 * *")
    public void runMonthlySnapshot() {
        log.info("🔥 Scheduler triggered at {}", LocalDateTime.now());

        YearMonth targetMonth = YearMonth.now().minusMonths(1);

        List<Long> companyIds = companyRepository.findActiveCompanyIds();

        for (Long companyId : companyIds) {

            boolean exists = snapshotService.exists(companyId, targetMonth);

            if (!exists) {
                snapshotService.calculateAndSave(companyId, targetMonth);
            }
        }
    }
}
