package com.example.account.receivable.BankReconciliation.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.BankReconciliation.Entity.BankTransaction;
import com.example.account.receivable.BankReconciliation.Enum.PaymentStatus;
import com.example.account.receivable.BankReconciliation.Repository.BankTransactionRepository;
import com.example.account.receivable.BankReconciliation.Utils.BaiCodeUtil;
import com.example.account.receivable.BankReconciliation.Utils.BaiTransactionInfo;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Enum.PaymentMethod;
import com.example.account.receivable.Payment.Enum.PaymentSource;
import com.example.account.receivable.Payment.Repository.PaymentRepository;
import com.example.account.receivable.Payment.Service.PaymentService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankReconciliationService {

    private final BankTransactionRepository bankTransactionRepository;
    private final CompanyRepository companyRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final PaymentService paymentService;

    @Transactional
    public void processBaiFile(MultipartFile file , Long companyId) {

        BankTransaction lastTransaction = null;

        try (BufferedReader reader =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.startsWith("16,")) {
                    lastTransaction = saveTransaction(line , companyId);
                }
                else if (line.startsWith("88,") && lastTransaction != null) {
                    appendContinuation(lastTransaction, line);
                }
            }

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid BAI file format"
            );
        }
    }

    private BankTransaction saveTransaction(String line , Long companyId) {

        Company company = companyRepository.findById(companyId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        String[] data = line.split(",");

        String baiCode = data[1];
        BigDecimal amount = new BigDecimal(data[2]);
        String reference = data.length > 4 ? data[4] : null;
        String customerName = data.length > 5 ? data[5] : null;
        String description = data.length > 6
                ? data[6].replace("/", "")
                : null;

    BaiTransactionInfo info = BaiCodeUtil.getInfo(baiCode);
    String systemNote = "Payment created via BAI file";

    BankTransaction transaction = BankTransaction.builder()
            .company(company) 
            .baiCode(baiCode)
            .transactionType(info.getTransactionType())
            .debitCredit(info.getDebitCredit())
            .amount(amount)
            .reference(reference)
            .customerName(customerName)
            .description(description)
            .transactionDate(LocalDate.now())
            .status(PaymentStatus.CREATED)
            .systemNote(systemNote)
            .source(PaymentSource.BANK)
            .build();

        return bankTransactionRepository.save(transaction);
    }

    private void appendContinuation(BankTransaction transaction, String line) {

        String continuationText = line
                .replace("88,", "")
                .replace("/", "");

        transaction.setDescription(
                transaction.getDescription() + " " + continuationText
        );

        bankTransactionRepository.save(transaction);
    }



    // Approve Bank Transaction and Apply on the Invoice 
    @Transactional
    public Payment approveAndApplyBankTransaction(
            Long bankTransactionId,
            Long customerId,
            List<Long> invoiceIds
    ) {
        BankTransaction bt = bankTransactionRepository.findById(bankTransactionId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank transaction not found"));

        if (bt.getStatus() != PaymentStatus.CREATED) {
            throw new IllegalStateException("Bank transaction already processed");
        }

        // Customer is explicitly selected by user
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Create Payment
        Payment payment = Payment.builder()
                .customer(customer)
                .bankDeposit(bt.getAmount())
                .paymentAmount(bt.getAmount())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .paymentDate(bt.getTransactionDate())
                .source(PaymentSource.BANK)
                .status(PaymentStatus.CREATED)
                .notes(bt.getDescription())
                .bankTransaction(bt)
                .build();

        payment = paymentRepository.save(payment);

        // Apply invoices
        paymentService.applyInvoices(payment, invoiceIds);

        // Final statuses
        payment.setStatus(PaymentStatus.APPROVED);
        bt.setStatus(PaymentStatus.APPLIED);

        paymentRepository.save(payment);
        bankTransactionRepository.save(bt);

        return payment;
    }

    //Get List of Bank Transaction
    public List<BankTransaction> getBankTransactions(
            Long companyId,
            LocalDate fromDate,
            LocalDate toDate,
            Integer months
    ) {
        validateDates(fromDate, toDate);

        LocalDate resolvedFrom = fromDate;
        LocalDate resolvedTo = toDate;

        if (resolvedFrom == null && resolvedTo == null && months != null && months > 0) {
            resolvedTo = LocalDate.now();
            resolvedFrom = resolvedTo.minusMonths(months);
        }

        return bankTransactionRepository.findByCompanyIdFiltered(
                companyId,
                PaymentStatus.CREATED,
                resolvedFrom,
                resolvedTo
        );
    }

    private void validateDates(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fromDate cannot be after toDate"
            );
        }
    }
}