package com.example.account.receivable.ERA.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.account.receivable.ERA.Entity.EraBatch;
import com.example.account.receivable.ERA.Entity.EraClaim;
import com.example.account.receivable.ERA.Enum.EraStatus;
import com.example.account.receivable.ERA.Repository.EraBatchRepository;
import com.example.account.receivable.ERA.Repository.EraClaimRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EraService {

    private final EraBatchRepository eraBatchRepository;
    private final EraClaimRepository eraClaimRepository;

    @Transactional
    public void processEraFile(MultipartFile file) {

        try {

            String content = new String(file.getBytes());

            // Split EDI by segment delimiter
            String[] segments = content.split("~");

            EraBatch.EraBatchBuilder batchBuilder = EraBatch.builder()
                    .rawFileName(file.getOriginalFilename())
                    .status(EraStatus.IMPORTED);

            EraBatch batch = null;
            EraClaim currentClaim = null;

            for (String segment : segments) {

                segment = segment.trim();
                if (segment.isEmpty()) continue;

                String[] parts = segment.split("\\*");
                if (parts.length == 0) continue;

                String segmentType = parts[0];

                // ---------------- BPR ----------------
                if ("BPR".equals(segmentType)) {

                    batchBuilder.totalPayment(new BigDecimal(parts[2]));

                    String dateStr = parts[parts.length - 1];

                    if (dateStr.matches("\\d{8}")) {
                        DateTimeFormatter formatter =
                                DateTimeFormatter.ofPattern("yyyyMMdd");

                        batchBuilder.paymentDate(
                                LocalDate.parse(dateStr, formatter)
                        );
                    }
                }

                // ---------------- TRN ----------------
                else if ("TRN".equals(segmentType)) {
                    batchBuilder.traceNumber(parts[2]);
                }

                // ---------------- PAYER ----------------
                else if ("N1".equals(segmentType)
                        && parts.length > 2
                        && "PR".equals(parts[1])) {

                    batchBuilder.payerName(parts[2]);
                }

                // ---------------- CLP (New Claim) ----------------
                else if ("CLP".equals(segmentType)) {

                    // SAVE previous claim before creating new one
                    if (currentClaim != null) {
                        eraClaimRepository.save(currentClaim);
                    }

                    if (batch == null) {
                        batch = eraBatchRepository.save(batchBuilder.build());
                    }

                    currentClaim = EraClaim.builder()
                            .batch(batch)
                            .claimNumber(parts[1])
                            .invoiceNumber(parts[7])
                            .billedAmount(new BigDecimal(parts[3]))
                            .paidAmount(new BigDecimal(parts[4]))
                            .allowedAmount(BigDecimal.ZERO)
                            .patientResponsibility(BigDecimal.ZERO)
                            .contractualAmount(BigDecimal.ZERO)
                            .rawClpSegment(segment)
                            .build();
                }

                // ---------------- AMT (Allowed Amount) ----------------
                else if ("AMT".equals(segmentType)
                        && parts.length > 2
                        && "AU".equals(parts[1])
                        && currentClaim != null) {

                    currentClaim.setAllowedAmount(
                            new BigDecimal(parts[2])
                    );
                }

                // ---------------- CAS (Adjustments) ----------------
                else if ("CAS".equals(segmentType)
                        && parts.length > 3
                        && currentClaim != null) {

                    String groupCode = parts[1];
                    BigDecimal amount = new BigDecimal(parts[3]);

                    if ("CO".equals(groupCode)) {
                        currentClaim.setContractualAmount(
                                currentClaim.getContractualAmount().add(amount)
                        );
                    }

                    if ("PR".equals(groupCode)) {
                        currentClaim.setPatientResponsibility(
                                currentClaim.getPatientResponsibility().add(amount)
                        );
                    }
                }
            }

            // SAVE last claim after loop
            if (currentClaim != null) {
                eraClaimRepository.save(currentClaim);
            }

            // SAVE batch if not already saved
            if (batch == null) {
                batch = eraBatchRepository.save(batchBuilder.build());
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid ERA file format: " + e.getMessage());
        }
    }
}
