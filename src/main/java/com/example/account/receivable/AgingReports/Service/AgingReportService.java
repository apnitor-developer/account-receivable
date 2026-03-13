package com.example.account.receivable.AgingReports.Service;

import com.example.account.receivable.AgingReports.DTO.AgingCodeResponse;
import com.example.account.receivable.AgingReports.DTO.AgingReportResponse;
import com.example.account.receivable.AgingReports.DTO.CreateAgingCodeRequest;
import com.example.account.receivable.AgingReports.DTO.CustomerAgingDto;
import com.example.account.receivable.AgingReports.Entity.AgingBucket;
import com.example.account.receivable.AgingReports.Repository.AgingBucketRepository;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AgingReportService {

    private final InvoiceRepository invoiceRepository;
    private final AgingBucketRepository agingBucketRepository;

    
    //Get Invoice Aging
    public AgingReportResponse getAgingReport(Long companyId, LocalDate asOfDate, Long customerId, String status) {

        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }

        // Load company aging buckets
        List<AgingBucket> buckets =
                agingBucketRepository.findByCompanyIdAndActiveTrueOrderByDisplayOrderAsc(companyId);

        if (buckets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No aging buckets configured for company");
        }

        // Get invoices
        List<Invoice> allOpenInvoices = invoiceRepository.findOpenInvoicesByCompany(companyId);

        List<Invoice> filtered = new ArrayList<>();

        for (Invoice inv : allOpenInvoices) {

            if (customerId != null) {
                Customer c = inv.getCustomer();
                if (c == null || !Objects.equals(c.getId(), customerId)) {
                    continue;
                }
            }

            if (status != null && !status.isBlank()) {

                InvoiceStatus invStatus = inv.getStatus();

                if (invStatus == null ||
                        !invStatus.name().equalsIgnoreCase(status)) {
                    continue;
                }
            }

            filtered.add(inv);
        }

        Map<Long, CustomerAgingDto> byCustomer = new LinkedHashMap<>();

        for (Invoice invoice : filtered) {

            Customer customer = invoice.getCustomer();
            if (customer == null) continue;

            Long cid = customer.getId();
            String cname = customer.getCustomerName();

            CustomerAgingDto row = byCustomer.computeIfAbsent(cid, id -> {

                CustomerAgingDto dto = new CustomerAgingDto();

                dto.setCustomerId(cid);
                dto.setCustomerName(cname);
                dto.setTotalDue(BigDecimal.ZERO);

                // Initialize all buckets
                Map<String, BigDecimal> bucketMap = new LinkedHashMap<>();

                for (AgingBucket bucket : buckets) {
                    bucketMap.put(bucket.getBucketName(), BigDecimal.ZERO);
                }

                dto.setBuckets(bucketMap);

                return dto;
            });

            BigDecimal balance = invoice.getBalanceDue();

            if (balance == null || balance.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            row.setTotalDue(row.getTotalDue().add(balance));

            LocalDate dueDate = invoice.getDueDate();

            long daysOverdue = 0;

            if (dueDate != null) {
                daysOverdue = ChronoUnit.DAYS.between(dueDate, asOfDate);
            }

            AgingBucket matchedBucket = null;

            for (AgingBucket bucket : buckets) {

                boolean matchStart = daysOverdue >= bucket.getStartDay();

                boolean matchEnd = bucket.getEndDay() == null
                        || daysOverdue <= bucket.getEndDay();

                if (matchStart && matchEnd) {
                    matchedBucket = bucket;
                    break;
                }
            }

            if (matchedBucket == null) {
                continue;
            }

            String bucketName = matchedBucket.getBucketName();

            BigDecimal currentValue = row.getBuckets().get(bucketName);

            row.getBuckets().put(
                    bucketName,
                    currentValue.add(balance)
            );
        }

        List<CustomerAgingDto> rows = new ArrayList<>(byCustomer.values());

        return new AgingReportResponse(asOfDate, rows);
    }

    //Create Aging Codes
    public AgingCodeResponse createAgingCode(Long companyId, CreateAgingCodeRequest request) {
        
        validateBucketRange(companyId,
            request.getStartDay(),
            request.getEndDay(),
            null);

        AgingBucket code = new AgingBucket();

        code.setCompanyId(companyId);
        code.setBucketName(request.getBucketName());
        code.setStartDay(request.getStartDay());
        code.setEndDay(request.getEndDay());
        code.setDisplayOrder(request.getDisplayOrder());
        code.setActive(true);
        code.setDelated(false);

        AgingBucket saved = agingBucketRepository.save(code);

        return map(saved);
    }

    //Validate Aging Code Bucket
    private void validateBucketRange(Long companyId, Integer startDay, Integer endDay, Long excludeId) {

        List<AgingBucket> existingBuckets =
                agingBucketRepository.findByCompanyIdAndActiveTrueOrderByDisplayOrderAsc(companyId);

        for (AgingBucket bucket : existingBuckets) {

            if (excludeId != null && bucket.getId().equals(excludeId)) {
                continue;
            }

            Integer existingStart = bucket.getStartDay();
            Integer existingEnd = bucket.getEndDay();

            if (existingEnd == null) {
                existingEnd = Integer.MAX_VALUE;
            }

            if (endDay == null) {
                endDay = Integer.MAX_VALUE;
            }

            boolean overlap =
                    startDay <= existingEnd && endDay >= existingStart;

            if (overlap) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Aging bucket overlaps with existing bucket: " + bucket.getBucketName()
                );
            }
        }
    }


    //Get Company Aging Code
    public List<AgingCodeResponse> getAgingCodes(Long companyId) {

        List<AgingBucket> list =
                agingBucketRepository.findByCompanyIdAndActiveTrueOrderByDisplayOrderAsc(companyId);

        return list.stream().map(this::map).toList();
    }


    //Update Aging Code
    public AgingCodeResponse updateAgingCode(Long id, CreateAgingCodeRequest request) {

        AgingBucket code = agingBucketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND , "Aging code not found"));

        if (request.getBucketName() != null) {
            code.setBucketName(request.getBucketName());
        }

        if (request.getStartDay() != null) {
            code.setStartDay(request.getStartDay());
        }

        if (request.getEndDay() != null) {
            code.setEndDay(request.getEndDay());
        }

        if (request.getDisplayOrder() != null) {
            code.setDisplayOrder(request.getDisplayOrder());
        }

        AgingBucket updated = agingBucketRepository.save(code);

        return map(updated);
    }


    //Delete Aging Code
    public void deleteAgingCode(Long id) {

        AgingBucket code = agingBucketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aging code not found"));

        code.setDelated(true);
        code.setActive(false);

        agingBucketRepository.save(code);
    }


    private AgingCodeResponse map(AgingBucket code) {
        return new AgingCodeResponse(
                code.getId(),
                code.getBucketName(),
                code.getStartDay(),
                code.getEndDay(),
                code.getDisplayOrder()
        );
    }
}