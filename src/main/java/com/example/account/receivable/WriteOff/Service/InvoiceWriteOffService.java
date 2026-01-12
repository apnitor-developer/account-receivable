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
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.WriteOff.Dto.CompanyWriteOffResponse;
import com.example.account.receivable.WriteOff.Dto.CreateWriteOffRequest;
import com.example.account.receivable.WriteOff.Repository.InvoiceWriteOffRepository;
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

    @Transactional
    public InvoiceWriteOff writeOffInvoice(
            Long companyId,
            Long invoiceId,
            CreateWriteOffRequest req
    ) {

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Company not found"
            ));


        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found")
                );

        if ("WRITTEN_OFF".equalsIgnoreCase(invoice.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invoice already written off"
            );
        }

        BigDecimal balanceDue = invoice.getBalanceDue();
        if (balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invoice has no balance to write off"
            );
        }
        

        ArCode arCode = null;
        if (req.getArCodeId() != null) {
            arCode = arCodeRepository.findById(req.getArCodeId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND, "AR code not found")
                    );
        }

        // Create write-off record
        InvoiceWriteOff writeOff = InvoiceWriteOff.builder()
                .invoice(invoice)
                .customer(invoice.getCustomer())
                .company(company)
                .reason(req.getReason())
                .arCode(arCode)
                .writeOffDate(LocalDate.now())
                .build();

        writeOffRepository.save(writeOff);

        // Update invoice
        invoice.setStatus("WRITTEN_OFF");
        invoiceRepository.save(invoice);

        return writeOff;
    }




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

