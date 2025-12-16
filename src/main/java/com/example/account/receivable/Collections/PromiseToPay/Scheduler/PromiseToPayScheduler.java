package com.example.account.receivable.Collections.PromiseToPay.Scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PromiseToPayScheduler {

    private final PromiseToPayRepo promiseToPayRepository;

    /**
     * Runs once every day at 12:05 AM
     */
    @Scheduled(cron = "0 5 0 * * ?")
    @Transactional
    public void updatePromiseStatuses() {

        LocalDate today = LocalDate.now();

        log.info("Running PromiseToPay status scheduler for date: {}", today);

        // 1️⃣ PENDING → DUE_TODAY
        List<PromiseToPay> dueToday =
                promiseToPayRepository
                        .findByPromiseDateAndStatus(today, PromiseStatus.PENDING);

        dueToday.forEach(promise ->
                promise.setStatus(PromiseStatus.DUE_TODAY)
        );

        // 2️⃣ PENDING / DUE_TODAY → BROKEN
        List<PromiseToPay> brokenPromises =
                promiseToPayRepository
                        .findByPromiseDateBeforeAndStatusIn(
                                today,
                                List.of(PromiseStatus.PENDING, PromiseStatus.DUE_TODAY)
                        );

        brokenPromises.forEach(promise ->
                promise.setStatus(PromiseStatus.BROKEN)
        );

        promiseToPayRepository.saveAll(dueToday);
        promiseToPayRepository.saveAll(brokenPromises);

        log.info("PromiseToPay status update completed. DueToday={}, Broken={}",
                dueToday.size(),
                brokenPromises.size()
        );
    }
}
