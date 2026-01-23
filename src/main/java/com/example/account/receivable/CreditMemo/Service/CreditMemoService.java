package com.example.account.receivable.CreditMemo.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.ArCodes.Repository.ArCodeRepository;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.CreditMemo.Dto.AppliedCreditMemoOnInvoiceResponse;
import com.example.account.receivable.CreditMemo.Dto.ApplyCreditMemoRequest;
import com.example.account.receivable.CreditMemo.Dto.CreateCreditMemoRequest;
import com.example.account.receivable.CreditMemo.Dto.CustomerCreditBalanceResponse;
import com.example.account.receivable.CreditMemo.Entity.CreditMemo;
import com.example.account.receivable.CreditMemo.Entity.CreditMemoApplication;
import com.example.account.receivable.CreditMemo.Repository.CreditMemoApplicationRepository;
import com.example.account.receivable.CreditMemo.Repository.CreditMemoRepository;
import com.example.account.receivable.CreditMemo.StatusFile.CreditMemoStatus;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.GL.Dto.GlTransactionCreateRequest;
import com.example.account.receivable.GL.Enum.GlReferenceType;
import com.example.account.receivable.GL.Service.GlTransactionService;
import com.example.account.receivable.HelperMethods.CompanyResolver;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreditMemoService {

    private final CreditMemoRepository creditMemoRepository;
    private final CustomerRepository customerRepository;
    private final ArCodeRepository arCodeRepository;
    private final CreditMemoApplicationRepository creditMemoApplicationRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;
    private final GlTransactionService glTransactionService;

    private static final String CM_PREFIX = "CM-";
    private static final int CM_NUMBER_WIDTH = 4;


    // Create Credit Memo
    @Transactional
    public CreditMemo createCreditMemo(Long customerId, CreateCreditMemoRequest req) {

        if (req.getAmount() == null || req.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be > 0");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        if (req.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

            if (!invoice.getCustomer().getId().equals(customerId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice does not belong to customer");
            }
        }

        ArCode arCode = null;
        if (req.getArCodeId() != null) {
            arCode = arCodeRepository.findById(req.getArCodeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "AR code not found"));
        }

        CreditMemo cm = CreditMemo.builder()
                .customer(customer)
                .creditMemoNo(generateUniqueCreditMemoNo())
                .creditReason(req.getCreditReason())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .status(CreditMemoStatus.DRAFT)                // ✅ DRAFT
                .targetInvoiceId(req.getInvoiceId()) // optional
                .arCode(arCode)
                .build();

        return creditMemoRepository.save(cm);
    }

    //Helper fn to generate CreditMemo Number
    private String generateUniqueCreditMemoNo() {
        int safety = 0;
        while (true) {
            safety++;
            String candidate = CM_PREFIX + String.format("%0" + CM_NUMBER_WIDTH + "d", (int)(Math.random() * 9999) + 1);
            if (!creditMemoRepository.existsByCreditMemoNo(candidate)) {
                return candidate;
            }
            if (safety > 50) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Unable to generate unique credit memo number");
            }
        }
    }




    // Approve Credit Memo 
    @Transactional
    public CreditMemo approveCreditMemo(Long creditMemoId) {

        CreditMemo cm = creditMemoRepository.findById(creditMemoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credit memo not found"));

        if (cm.getStatus() != CreditMemoStatus.DRAFT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only DRAFT credit memos can be approved");
        }

        cm.setStatus(CreditMemoStatus.APPROVED);
        cm.setPostingDate(LocalDate.now());

        // 👇 Apply to invoice ONLY now
        if (cm.getTargetInvoiceId() != null) {

            Invoice invoice = invoiceRepository.findById(cm.getTargetInvoiceId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

            BigDecimal invoiceDue = invoice.getBalanceDue();
            if (invoiceDue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice has no balance due");
            }

            BigDecimal applyAmount = cm.getAmount().min(invoiceDue);

            // reduce invoice
            invoice.setBalanceDue(invoiceDue.subtract(applyAmount));
            updateInvoiceStatus(invoice);
            invoiceRepository.save(invoice);

            // create application record
            CreditMemoApplication app = CreditMemoApplication.builder()
                    .creditMemo(cm)
                    .invoice(invoice)
                    .appliedAmount(applyAmount)
                    .appliedDate(LocalDate.now())
                    .build();

            creditMemoApplicationRepository.save(app);
        }


            // SAVE TRANSACTION
            glTransactionService.createTransaction(
                CompanyResolver
                    .resolveCompanyForCustomer(cm.getCustomer())
                    .getId(),
                GlTransactionCreateRequest.builder()
                    .referenceType(GlReferenceType.CREDIT_MEMO)
                    .referenceId(cm.getId())
                    .referenceNumber(cm.getCreditMemoNo())
                    .amount(cm.getAmount())
                    .transactionDate(cm.getPostingDate())
                    .description("Credit memo approved: " + cm.getCreditMemoNo())
                    .build()
            );

        return creditMemoRepository.save(cm);
    }




    // Get Company Credit Memos By Status
    public Page<CreditMemo> getCompanyCreditMemosByStatus(
            Long companyId,
            CreditMemoStatus status,
            int page,
            int size
    ) {
        companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Company not found"
                        )
                );

        Pageable pageable = PageRequest.of(page, size);

        return creditMemoRepository.findCompanyCreditMemos(
                companyId,
                status,
                pageable
        );
    }




    // Get Customer CreditMemo Balance
    public CustomerCreditBalanceResponse getCustomerCreditBalance(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")
                );

        BigDecimal totalPosted =
                creditMemoRepository.getCustomerTotalCreditsByStatus(customerId , CreditMemoStatus.APPROVED);

        BigDecimal totalApplied =
                creditMemoApplicationRepository.getCustomerTotalApplied(customerId);

        if (totalPosted == null) totalPosted = BigDecimal.ZERO;
        if (totalApplied == null) totalApplied = BigDecimal.ZERO;

        BigDecimal available = totalPosted.subtract(totalApplied);

        return CustomerCreditBalanceResponse.builder()
                .customerId(customer.getId())
                .customerName(customer.getCustomerName())
                .totalPostedCredit(totalPosted)
                .totalAppliedCredit(totalApplied)
                .availableCredit(available)
                .currency("INR") // or derive from customer / credit memo
                .build();
    }



    // Apply CreditMemo on the Customer Invoice
    @Transactional
    public CreditMemoApplication applyToInvoice(Long creditMemoId, ApplyCreditMemoRequest req) {

        if (req.getInvoiceId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invoiceId is required");
        }

        CreditMemo cm = creditMemoRepository.findById(creditMemoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Credit memo not found"));

        // Since you're keeping simple flow, CM is always Posted.
        if (cm.getStatus() != CreditMemoStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Credit memo must be APPROVED");
        }

        Invoice invoice = invoiceRepository.findById(req.getInvoiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        // must belong to same customer
        if (!invoice.getCustomer().getId().equals(cm.getCustomer().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice customer does not match credit memo customer");
        }

        BigDecimal invoiceDue = invoice.getBalanceDue() == null ? BigDecimal.ZERO : invoice.getBalanceDue();
        if (invoiceDue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invoice has no balance due");
        }

        BigDecimal appliedTotal = creditMemoApplicationRepository.getAppliedTotal(creditMemoId);
        if (appliedTotal == null) appliedTotal = BigDecimal.ZERO;

        BigDecimal remainingCredit = cm.getAmount().subtract(appliedTotal);
        if (remainingCredit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No remaining credit to apply");
        }

        BigDecimal applyAmount = req.getAmount();
        if (applyAmount == null) {
            applyAmount = remainingCredit.min(invoiceDue); // auto apply
        }

        if (applyAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply amount must be > 0");
        }
        if (applyAmount.compareTo(remainingCredit) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply amount exceeds remaining credit");
        }
        if (applyAmount.compareTo(invoiceDue) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apply amount exceeds invoice balance due");
        }

        // reduce invoice due
        BigDecimal newDue = invoiceDue.subtract(applyAmount);
        invoice.setBalanceDue(newDue);

        // update invoice status OPEN/PARTIAL/PAID
        updateInvoiceStatus(invoice);

        invoiceRepository.save(invoice);

        CreditMemoApplication app = CreditMemoApplication.builder()
                .creditMemo(cm)
                .invoice(invoice)
                .appliedAmount(applyAmount)
                .appliedDate(LocalDate.now())
                .build();

        return creditMemoApplicationRepository.save(app);
    }

    private void updateInvoiceStatus(Invoice invoice) {
        BigDecimal due = invoice.getBalanceDue() == null ? BigDecimal.ZERO : invoice.getBalanceDue();
        BigDecimal total = invoice.getTotalAmount() == null ? BigDecimal.ZERO : invoice.getTotalAmount();

        if (due.compareTo(BigDecimal.ZERO) == 0) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setLastPaymentDate(LocalDate.now()); // optional; it's not cash payment, but marks settlement
        } else if (due.compareTo(total) < 0) {
            invoice.setStatus(InvoiceStatus.PARTIAL);
        } else {
            invoice.setStatus(InvoiceStatus.OPEN);
        }
    }



    //Get Company Appply Credit Memos
    public Page<AppliedCreditMemoOnInvoiceResponse> getCompanyAppliedCreditMemos(
            Long companyId, int page, int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<CreditMemoApplication> apps =
                creditMemoApplicationRepository.findCompanyAppliedCreditMemos(companyId, pageable);

        return apps.map(a -> {
            CreditMemo cm = a.getCreditMemo();
            Invoice inv = a.getInvoice();
            Customer cust = inv.getCustomer();

            AppliedCreditMemoOnInvoiceResponse dto =
                    new AppliedCreditMemoOnInvoiceResponse();

            dto.setApplicationId(a.getId());
            dto.setCreditMemoId(cm.getId());
            dto.setCreditMemoNo(cm.getCreditMemoNo());
            dto.setCreditReason(cm.getCreditReason());
            dto.setCurrency(cm.getCurrency());
            dto.setPostingDate(cm.getPostingDate());

            dto.setAppliedAmount(a.getAppliedAmount());
            dto.setAppliedDate(a.getAppliedDate());

            dto.setInvoiceId(inv.getId());
            dto.setInvoiceNumber(inv.getInvoiceNumber());

            dto.setCustomerId(cust.getId());
            dto.setCustomerName(cust.getCustomerName());

            if (cm.getArCode() != null) {
                dto.setArCodeId(cm.getArCode().getId());
                dto.setArCode(cm.getArCode().getCode());
                dto.setArCodeName(cm.getArCode().getName());
            }

            return dto;
        });
    }

}

