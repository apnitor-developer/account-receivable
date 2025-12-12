package com.example.account.receivable.Collections.PromiseToPay.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;

public interface PromiseToPayRepo extends JpaRepository<PromiseToPay, Long> {

    List<PromiseToPay> findByCustomerId(Long customerId);

    List<PromiseToPay> findByStatus(PromiseStatus status);

    List<PromiseToPay> findByStatusIn(List<PromiseStatus> statuses);

    List<PromiseToPay> findByPromiseDateBeforeAndStatus(LocalDate date, PromiseStatus status);
}
