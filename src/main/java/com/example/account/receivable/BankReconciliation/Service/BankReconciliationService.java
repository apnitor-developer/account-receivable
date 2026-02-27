package com.example.account.receivable.BankReconciliation.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
import com.example.account.receivable.ERA.Entity.EraBatch;
import com.example.account.receivable.ERA.Entity.EraClaim;
import com.example.account.receivable.ERA.Entity.EraClaimApplication;
import com.example.account.receivable.ERA.Enum.EraStatus;
import com.example.account.receivable.ERA.Repository.EraBatchRepository;
import com.example.account.receivable.ERA.Repository.EraClaimApplicationRepository;
import com.example.account.receivable.ERA.Repository.EraClaimRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Entity.InvoiceItem;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Enum.InvoiceType;
import com.example.account.receivable.Invoice.Repository.InvoiceItemRepo;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Invoice.Service.InvoiceService;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Enum.PayerType;
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
    private final InvoiceRepository invoiceRepository;
    private final EraBatchRepository eraBatchRepository;
    private final EraClaimRepository eraClaimRepository;
    private final EraClaimApplicationRepository eraClaimApplicationRepository;
    private final InvoiceService invoiceService;
    private final InvoiceItemRepo invoiceItemRepository;

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

        transaction = bankTransactionRepository.save(transaction);

        // attempt auto apply
        tryAutoApply(transaction, companyId);

        return transaction;
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


//     //Approve ERA Payment
//     @Transactional
//     public void approveWithEra(Long bankTransactionId, Long companyId) {

//             BankTransaction bt = bankTransactionRepository.findById(bankTransactionId)
//                             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
//                                             "Bank transaction not found"));

//             if (bt.getStatus() != PaymentStatus.CREATED) {
//                     throw new IllegalStateException("Bank transaction already processed");
//             }

//             EraBatch batch = eraBatchRepository
//                             .findByPayerNameAndTotalPayment(
//                                             bt.getCustomerName(),
//                                             bt.getAmount())
//                             .orElseThrow(() -> new RuntimeException("No matching ERA found"));

//             if (batch.getStatus() == EraStatus.FUNDED) {
//                     throw new RuntimeException("ERA already funded");
//             }

//             List<EraClaim> claims = eraClaimRepository.findByBatch_Id(batch.getId());

//             BigDecimal totalApplied = BigDecimal.ZERO;

//             for (EraClaim claim : claims) {

//                     Invoice invoice = invoiceRepository
//                                     .findByInvoiceNumber(claim.getInvoiceNumber())
//                                     .orElseThrow(() -> new RuntimeException("Invoice not found"));

//                     Customer customer = invoice.getCustomer();

//                     BigDecimal paidAmount = claim.getPaidAmount();

//                     System.err.println("Paid Amount" + paidAmount);

//                     if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) <= 0) {
//                             continue;
//                     }

//                     totalApplied = totalApplied.add(paidAmount);

//                     // Create one Payment per invoice
//                     Payment payment = Payment.builder()
//                                     .customer(customer)
//                                     .bankDeposit(paidAmount)
//                                     .paymentAmount(paidAmount)
//                                     .paymentMethod(PaymentMethod.BANK_TRANSFER)
//                                     .paymentDate(bt.getTransactionDate())
//                                     .source(PaymentSource.BANK)
//                                     .payerType(PayerType.INSURANCE)
//                                     .payerName(batch.getPayerName())
//                                     .status(PaymentStatus.CREATED)
//                                     .bankTransaction(bt)
//                                     .build();

//                     payment = paymentRepository.save(payment);

//                     // Apply contractual adjustment
//                     invoice.setBalanceDue(
//                                     invoice.getBalanceDue()
//                                                     .subtract(claim.getContractualAmount()));

//                     paymentService.applyExactAmount(
//                                     payment,
//                                     invoice,
//                                     paidAmount);

//                     invoiceRepository.save(invoice);

//                     payment.setStatus(PaymentStatus.APPROVED);
//                     paymentRepository.save(payment);

//                     // Save ERA mapping
//                     EraClaimApplication app = new EraClaimApplication();
//                     app.setEraClaim(claim);
//                     app.setInvoice(invoice);
//                     eraClaimApplicationRepository.save(app);
//             }

//             if (totalApplied.compareTo(bt.getAmount()) != 0) {
//                     throw new RuntimeException("ERA total does not match bank amount");
//             }

//             bt.setStatus(PaymentStatus.APPLIED);
//             batch.setStatus(EraStatus.FUNDED);

//             bankTransactionRepository.save(bt);
//             eraBatchRepository.save(batch);
//     }




@Transactional
public void approveWithEraAndCreatePatientInvoice(
        Long bankTransactionId,
        Long companyId
) {

    // 1️⃣ Get Bank Transaction
    BankTransaction bt = bankTransactionRepository.findById(bankTransactionId)
            .orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Bank transaction not found"));

    if (bt.getStatus() != PaymentStatus.CREATED) {
        throw new IllegalStateException("Bank transaction already processed");
    }

    // 2️⃣ Find matching ERA batch
    EraBatch batch = eraBatchRepository
            .findByPayerNameAndTotalPayment(
                    bt.getCustomerName(),
                    bt.getAmount()
            )
            .orElseThrow(() ->
                    new RuntimeException("No matching ERA found"));

    if (batch.getStatus() == EraStatus.FUNDED) {
        throw new RuntimeException("ERA already funded");
    }

    List<EraClaim> claims =
            eraClaimRepository.findByBatch_Id(batch.getId());

    if (claims.isEmpty()) {
        throw new RuntimeException("No ERA claims found");
    }

    // 3️⃣ Group ERA claims by CUSTOMER ID (SAFE VERSION)
    Map<Long, List<EraClaim>> claimsByCustomer = new HashMap<>();

    for (EraClaim claim : claims) {

        Invoice invoice = invoiceRepository
                .findByInvoiceNumber(claim.getInvoiceNumber())
                .orElseThrow(() ->
                        new RuntimeException("Invoice not found: "
                                + claim.getInvoiceNumber()));

        Long customerId = invoice.getCustomer().getId();

        claimsByCustomer
                .computeIfAbsent(customerId, k -> new ArrayList<>())
                .add(claim);
    }

    BigDecimal grandTotalApplied = BigDecimal.ZERO;

    // 4️⃣ Process each customer separately
    for (Map.Entry<Long, List<EraClaim>> entry : claimsByCustomer.entrySet()) {

        Long customerId = entry.getKey();

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        List<EraClaim> customerClaims = entry.getValue();

        BigDecimal customerTotal = BigDecimal.ZERO;

        // Calculate total paid for this customer
        for (EraClaim claim : customerClaims) {

            BigDecimal paid =
                    claim.getPaidAmount() != null
                            ? claim.getPaidAmount()
                            : BigDecimal.ZERO;

            customerTotal = customerTotal.add(paid);
        }

        if (customerTotal.compareTo(BigDecimal.ZERO) <= 0) {
            continue;
        }

        // 5️⃣ Create Payment for this customer
        Payment payment = Payment.builder()
                .customer(customer)
                .bankDeposit(customerTotal)
                .paymentAmount(customerTotal)
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .paymentDate(bt.getTransactionDate())
                .source(PaymentSource.BANK)
                .payerType(PayerType.INSURANCE)
                .payerName(batch.getPayerName())
                .status(PaymentStatus.CREATED)
                .bankTransaction(bt)
                .build();

        payment = paymentRepository.save(payment);

        Set<Long> processedInvoices = new HashSet<>();

        // 6️⃣ Apply each claim for this customer
        for (EraClaim claim : customerClaims) {

            Invoice invoice = invoiceRepository
                    .findByInvoiceNumber(claim.getInvoiceNumber())
                    .orElseThrow(() ->
                            new RuntimeException("Invoice not found: "
                                    + claim.getInvoiceNumber()));

            BigDecimal paidAmount =
                    claim.getPaidAmount() != null
                            ? claim.getPaidAmount()
                            : BigDecimal.ZERO;

            BigDecimal contractual =
                    claim.getContractualAmount() != null
                            ? claim.getContractualAmount()
                            : BigDecimal.ZERO;

            // Apply contractual adjustment
            if (contractual.compareTo(BigDecimal.ZERO) > 0) {
                invoice.setBalanceDue(
                        invoice.getBalanceDue().subtract(contractual)
                );
            }

            // Apply insurance payment
            if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {

                paymentService.applyExactAmount(
                        payment,
                        invoice,
                        paidAmount
                );

                grandTotalApplied = grandTotalApplied.add(paidAmount);
            }

            invoiceRepository.save(invoice);

            // 7️⃣ Create patient invoice if balance remains
            if (!processedInvoices.contains(invoice.getId())
                    && invoice.getBalanceDue().compareTo(BigDecimal.ZERO) > 0) {

                processedInvoices.add(invoice.getId());

                BigDecimal patientBalance = invoice.getBalanceDue();

                invoice.setStatus(InvoiceStatus.WRITTEN_OFF);
                invoiceRepository.save(invoice);

                Invoice patientInvoice = Invoice.builder()
                        .invoiceNumber(invoice.getInvoiceNumber() + "-P")
                        .invoiceDate(LocalDate.now())
                        .dueDate(LocalDate.now().plusDays(30))
                        .customer(invoice.getCustomer())
                        .subTotal(patientBalance)
                        .totalAmount(patientBalance)
                        .balanceDue(patientBalance)
                        .status(InvoiceStatus.OPEN)
                        .invoiceType(InvoiceType.PATIENT)
                        .parentInvoice(invoice)
                        .generated(true)
                        .active(true)
                        .deleted(false)
                        .build();

                patientInvoice = invoiceRepository.save(patientInvoice);

                InvoiceItem item = InvoiceItem.builder()
                        .itemName("Patient Responsibility")
                        .description("Remaining balance after insurance payment")
                        .quantity(1)
                        .rate(patientBalance)
                        .amount(patientBalance)
                        .taxAmount(BigDecimal.ZERO)
                        .total(patientBalance)
                        .invoice(patientInvoice)
                        .build();

                invoiceItemRepository.save(item);

                invoiceService.sendInvoiceEmail(
                        patientInvoice.getId(),
                        companyId
                );
            }

            // Save ERA mapping
            EraClaimApplication app = new EraClaimApplication();
            app.setEraClaim(claim);
            app.setInvoice(invoice);
            eraClaimApplicationRepository.save(app);
        }

        // Approve this customer's payment
        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepository.save(payment);
    }

    // 8️⃣ Validate totals
    if (grandTotalApplied.compareTo(bt.getAmount()) != 0) {
        throw new RuntimeException(
                "ERA total does not match bank amount"
        );
    }

    // 9️⃣ Final status updates
    bt.setStatus(PaymentStatus.APPLIED);
    batch.setStatus(EraStatus.FUNDED);

    bankTransactionRepository.save(bt);
    eraBatchRepository.save(batch);
}




    // Get List of Bank Transaction
    public List<BankTransaction> getBankTransactions(
                    Long companyId,
                    LocalDate fromDate,
                    LocalDate toDate,
                    Integer months) {
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
                            resolvedTo);
    }

    private void validateDates(LocalDate fromDate, LocalDate toDate) {
            if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
                    throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "fromDate cannot be after toDate");
            }
    }



    // Match Customer with the BAI file Customer Name
    private Optional<Customer> matchCustomer(
        BankTransaction bt,
        Long companyId
    ) {
        if (bt.getCustomerName() == null) {
            return Optional.empty();
        }

        return customerRepository.findByCustomerNameAndCompany(
                bt.getCustomerName(),
                companyId
        );
    }


    //Auto Apply on the Invoice when Upload the BAI File
    @Transactional
    public void tryAutoApply(BankTransaction bt, Long companyId) {

        Optional<Customer> customerOpt = matchCustomer(bt, companyId);

        if (customerOpt.isEmpty()) {
            return;
        }

        Customer customer = customerOpt.get();

        List<Invoice> openInvoices =
                invoiceRepository.findOpenInvoicesByCustomer(customer.getId());

        if (openInvoices.isEmpty()) {
            return;
        }

        Payment payment = Payment.builder()
                .customer(customer)
                .bankDeposit(bt.getAmount())
                .paymentAmount(bt.getAmount())
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .paymentDate(bt.getTransactionDate())
                .source(PaymentSource.BANK)
                .status(PaymentStatus.CREATED)
                .notes("Auto-applied from BAI upload")
                .bankTransaction(bt)
                .build();

        payment = paymentRepository.save(payment);

        // apply invoices (oldest first)
        List<Long> invoiceIds = openInvoices.stream()
                .map(Invoice::getId)
                .toList();

        paymentService.applyInvoices(payment, invoiceIds);

        payment.setStatus(PaymentStatus.APPROVED);
        bt.setStatus(PaymentStatus.APPLIED);

        paymentRepository.save(payment);
        bankTransactionRepository.save(bt);
    }

}