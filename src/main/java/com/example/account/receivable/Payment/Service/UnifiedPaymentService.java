package com.example.account.receivable.Payment.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;
import com.example.account.receivable.BankReconciliation.Repository.BankTransactionRepository;
import com.example.account.receivable.Payment.Dto.UnifiedPaymentDto;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnifiedPaymentService {

    private final BankTransactionRepository bankTransactionRepository;
    private final PaymentRepository paymentRepository;

    public List<UnifiedPaymentDto> getCompanyPayments(Long companyId) {

        List<UnifiedPaymentDto> result = new ArrayList<>();

        // 1️⃣ BAI transactions
        List<BankTransaction> baiTxns =
                bankTransactionRepository.findByCompany_Id(companyId);

        for (BankTransaction tx : baiTxns) {
            result.add(
                UnifiedPaymentDto.builder()
                    .id(tx.getId())
                    .date(tx.getTransactionDate())
                    .amount(tx.getAmount())
                    .customerName(tx.getCustomerName())
                    .description(tx.getDescription())
                    .source("BAI")
                    .status(tx.getStatus().name())
                    .build()
            );
        }

        // 2️⃣ Manual payments
        List<Payment> payments =
                paymentRepository.findPaymentsByCompanyId(
                        companyId,
                        Pageable.unpaged()
                ).getContent();

        for (Payment p : payments) {
            result.add(
                UnifiedPaymentDto.builder()
                    .id(p.getId())
                    .date(p.getPaymentDate())
                    .amount(p.getPaymentAmount())
                    .customerName(p.getCustomer().getCustomerName())
                    .description(p.getNotes())
                    .source("MANUAL")
                    .status("COMPLETED")
                    .build()
            );
        }

        // 3️⃣ Sort by date (latest first)
        result.sort(
            Comparator.comparing(UnifiedPaymentDto::getDate).reversed()
        );

        return result;
    }
}

