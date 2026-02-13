package com.example.account.receivable.PaymentTerms.Seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.account.receivable.PaymentTerms.Entity.PaymentTerm;
import com.example.account.receivable.PaymentTerms.Repository.PaymentTermRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentTermSeeder implements CommandLineRunner {

    private final PaymentTermRepository repository;

    @Override
    public void run(String... args) {

        if (repository.count() > 0) return;

        repository.save(PaymentTerm.builder()
                .name("Due on Receipt")
                .netDays(0)
                .active(true)
                .build());

        repository.save(PaymentTerm.builder()
                .name("Net 30")
                .netDays(30)
                .active(true)
                .build());

        repository.save(PaymentTerm.builder()
                .name("Net 60")
                .netDays(60)
                .active(true)
                .build());

        repository.save(PaymentTerm.builder()
                .name("Net 90")
                .netDays(90)
                .active(true)
                .build());
    }
}
