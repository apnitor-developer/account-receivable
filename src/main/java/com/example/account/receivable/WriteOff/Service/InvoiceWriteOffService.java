package com.example.account.receivable.WriteOff.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.ArCodes.Entity.ArCode;
import com.example.account.receivable.ArCodes.Repository.ArCodeRepository;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.GL.Dto.GlTransactionCreateRequest;
import com.example.account.receivable.GL.Enum.GlReferenceType;
import com.example.account.receivable.GL.Service.GlTransactionService;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.WriteOff.Dto.CompanyWriteOffResponse;
import com.example.account.receivable.WriteOff.Dto.CreateWriteOffRequest;
import com.example.account.receivable.WriteOff.Repository.InvoiceWriteOffRepository;
import com.example.account.receivable.WriteOff.StatusFile.WriteOffStatus;
import com.example.account.receivable.WriteOff.Entity.InvoiceWriteOff;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceWriteOffService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceWriteOffRepository writeOffRepository;
    private final ArCodeRepository arCodeRepository;
    private final CompanyRepository companyRepository;
    private final GlTransactionService glTransactionService;


    // Create Write Off Invoice
    @Transactional
    public InvoiceWriteOff writeOffInvoice( Long companyId, Long invoiceId, CreateWriteOffRequest req) {

            Company company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.NOT_FOUND, "Company not found"));

            Invoice invoice = invoiceRepository.findById(invoiceId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

            // DO NOT change invoice status here
            if (invoice.getStatus() == InvoiceStatus.WRITTEN_OFF) {
                    throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Invoice already written off");
            }

            if (invoice.getBalanceDue().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Invoice has no balance to write off");
            }

            ArCode arCode = null;
            if (req.getArCodeId() != null) {
                    arCode = arCodeRepository.findById(req.getArCodeId())
                                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                    "AR code not found"));
            }

            InvoiceWriteOff writeOff = InvoiceWriteOff.builder()
                            .invoice(invoice)
                            .customer(invoice.getCustomer())
                            .company(company)
                            .reason(req.getReason())
                            .arCode(arCode)
                            .status(WriteOffStatus.CREATED)
                            .writeOffDate(LocalDate.now())
                            .build();

            return writeOffRepository.save(writeOff);
    }




    // Approve Write off Invoice
    @Transactional
    public InvoiceWriteOff approveWriteOff(Long writeOffId) {

            InvoiceWriteOff writeOff = writeOffRepository.findById(writeOffId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                            "Write-off not found"));

            if (writeOff.getStatus() == WriteOffStatus.APPROVED) {
                    throw new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Write-off already approved");
            }

            // Update write-off status
            writeOff.setStatus(WriteOffStatus.APPROVED);

            // Update invoice status
            Invoice invoice = writeOff.getInvoice();
            invoice.setStatus(InvoiceStatus.WRITTEN_OFF);

            invoiceRepository.save(invoice);

            // SAVE TRANSACTION
            glTransactionService.createTransaction(
                            writeOff.getCompany().getId(),
                            GlTransactionCreateRequest.builder()
                                            .referenceType(GlReferenceType.WRITE_OFF)
                                            .referenceId(writeOff.getId())
                                            .referenceNumber("WO-" + writeOff.getId())
                                            .amount(invoice.getBalanceDue())
                                            .transactionDate(writeOff.getWriteOffDate())
                                            .description("Invoice written off: " + invoice.getInvoiceNumber())
                                            .build());

            return writeOffRepository.save(writeOff);
    }



    //Get Writeoff List based on the status
    public Page<CompanyWriteOffResponse> getCompanyWriteOffsByStatus(
                    Long companyId,
                    WriteOffStatus status,
                    int page,
                    int size) {
            companyRepository.findById(companyId)
                            .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Company not found"));

            Pageable pageable = PageRequest.of(page, size);

            Page<InvoiceWriteOff> writeOffs = writeOffRepository.findByCompanyIdAndStatus(
                            companyId,
                            status,
                            pageable);

            return writeOffs.map(w -> {
                    CompanyWriteOffResponse dto = new CompanyWriteOffResponse();
                    dto.setId(w.getId());
                    dto.setInvoiceId(w.getInvoice().getId());
                    dto.setInvoiceNumber(w.getInvoice().getInvoiceNumber());
                    dto.setCustomerId(w.getCustomer().getId());
                    dto.setCustomerName(w.getCustomer().getCustomerName());
                    dto.setReason(w.getReason());
                    dto.setWriteOffDate(w.getWriteOffDate());
                    dto.setStatus(w.getStatus().name());
                    return dto;
            });
    }



    //get all write-off list
    public Page<CompanyWriteOffResponse> getCompanyWriteOffs(
            Long companyId,
            int page,
            int size
    ) {
        // Validate company exists
        companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Company not found"
                        )
                );

        Pageable pageable = PageRequest.of(page, size);

        Page<InvoiceWriteOff> writeOffs =
                writeOffRepository.findCompanyWriteOffs(companyId, pageable);

        return writeOffs.map(w -> {
            CompanyWriteOffResponse dto = new CompanyWriteOffResponse();
            dto.setId(w.getId());
            dto.setInvoiceId(w.getInvoice().getId());
            dto.setInvoiceNumber(w.getInvoice().getInvoiceNumber());
            dto.setCustomerId(w.getCustomer().getId());
            dto.setCustomerName(w.getCustomer().getCustomerName());
            dto.setReason(w.getReason());
            dto.setWriteOffDate(w.getWriteOffDate());
            return dto;
        });
    }
}

