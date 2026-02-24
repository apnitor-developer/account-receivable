package com.example.account.receivable.Collections.Dispute.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Collections.Dispute.DTO.DisputeCodeResponseDto;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeDTORequest;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeStatusUpdateRequest;
import com.example.account.receivable.Collections.Dispute.Entity.Dispute;
import com.example.account.receivable.Collections.Dispute.Enum.DisputeCode;
import com.example.account.receivable.Collections.Dispute.Enum.DisputeStatus;
import com.example.account.receivable.Collections.Dispute.Repository.DisputeRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DisputeService {
    
    private final DisputeRepository disputeRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private static final String DISPUTE_PREFIX = "DSP-";
    private static final int DISPUTE_NUMBER_WIDTH = 4;

    public Dispute createDispute(DisputeDTORequest dto) {

        // Fetch Customer
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));


        // Fetch Invoice
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found"));


        // Validate ownership
        if (!invoice.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("Invoice does not belong to this customer");
        }

        BigDecimal disputedAmt = new BigDecimal(dto.getDisputedAmount());

        if (disputedAmt.compareTo(invoice.getTotalAmount()) > 0) {
            throw new RuntimeException("Disputed amount cannot exceed invoice amount");
        }


        // Generate DSP-xxx ID
        String disputeBusinessId = generateUniqueDisputeId();


        // Create Dispute
        Dispute dispute = Dispute.builder()
                .disputeId(disputeBusinessId)
                .customer(customer)
                .invoice(invoice)
                .invoiceOriginalAmount(invoice.getTotalAmount())
                .disputedAmount(disputedAmt)
                .disputeCode(DisputeCode.valueOf(dto.getDisputeCode()))
                .reason(dto.getReason())
                .status(DisputeStatus.OPEN)
                .build();

        return disputeRepository.save(dispute);
    }



    // Get Dispute by Id
    public Dispute getByDisputeId(Long disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));
    }


    // Get Dispute Codes
    public List<DisputeCodeResponseDto> getDisputeCodes() {
        return Arrays.stream(DisputeCode.values())
                .map(code -> new DisputeCodeResponseDto(
                        code.getCode(),
                        code.getLabel()
                ))
                .toList();
    }


    // Get Copmany Dispute List
    public List<Dispute> getCompanyDisputeList(Long companyId) {
    if (companyId == null) {
        throw new RuntimeException("Company ID must not be null");
    }

        return disputeRepository.findAllByCompanyId(companyId);
    }


    // DisputeId Auto Generator method
    private String generateUniqueDisputeId() {
        String prefix = DISPUTE_PREFIX;

        // Get the last dispute ID starting with "DSP-"
        var lastOpt = disputeRepository
                .findTopByDisputeIdStartingWithOrderByDisputeIdDesc(prefix);

        int nextNumber = 1; // default if none exist

        if (lastOpt.isPresent()) {
            String lastNumber = lastOpt.get().getDisputeId(); // e.g. "DSP-0042"
            String[] parts = lastNumber.split("-");
            if (parts.length == 2) {
                try {
                    int current = Integer.parseInt(parts[1]);
                    if (current >= 9999) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Maximum dispute number (DSP-9999) reached"
                        );
                    }
                    nextNumber = current + 1;
                } catch (NumberFormatException ignore) {
                    nextNumber = 1;
                }
            }
        }

        // Format as 4-digit number with leading zeros
        String formatted = String.format("%0" + DISPUTE_NUMBER_WIDTH + "d", nextNumber);
        String candidate = prefix + formatted; // e.g. "DSP-0007"

        // Safety check for rare race conditions
        int safetyCounter = 0;
        while (disputeRepository.existsByDisputeId(candidate)) {
            safetyCounter++;
            if (safetyCounter > 20) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Unable to generate unique dispute ID"
                );
            }

            nextNumber++;
            if (nextNumber > 9999) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Maximum dispute number (DSP-9999) reached"
                );
            }

            formatted = String.format("%0" + DISPUTE_NUMBER_WIDTH + "d", nextNumber);
            candidate = prefix + formatted;
        }

        return candidate;
    }



    // Update Dispute Status
    public Dispute updateDisputeStatus(Long disputeId, DisputeStatusUpdateRequest dto) {

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            throw new RuntimeException("Status must not be empty");
        }

        DisputeStatus newStatus;
        try {
            newStatus = DisputeStatus.valueOf(dto.getStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Invalid dispute status: " + dto.getStatus());
        }

        // Optional: block updates after closed
        if (dispute.getStatus() == DisputeStatus.CLOSED) {
            throw new RuntimeException("Closed dispute status cannot be changed");
        }

        // Optional: validate transitions (recommended)
        validateDisputeStatusTransition(dispute.getStatus(), newStatus);

        dispute.setStatus(newStatus);
        return disputeRepository.save(dispute);
    }



    //Validate transitions
    private void validateDisputeStatusTransition(DisputeStatus current, DisputeStatus next) {

        if (current == next) return;

        switch (current) {
            case OPEN -> {
                if (!(next == DisputeStatus.UNDER_REVIEW || next == DisputeStatus.CLOSED)) {
                    throw new RuntimeException("OPEN can move only to UNDER_REVIEW or CLOSED");
                }
            }
            case UNDER_REVIEW -> {
                if (!(next == DisputeStatus.RESOLVED || next == DisputeStatus.REJECTED || next == DisputeStatus.CLOSED)) {
                    throw new RuntimeException("UNDER_REVIEW can move only to RESOLVED, REJECTED, or CLOSED");
                }
            }
            case RESOLVED, REJECTED -> {
                if (next != DisputeStatus.CLOSED) {
                    throw new RuntimeException(current + " can move only to CLOSED");
                }
            }
            case CLOSED -> throw new RuntimeException("CLOSED status cannot be changed");
        }
    }


}
