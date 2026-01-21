package com.example.account.receivable.GL.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.GL.Dto.GlTransactionCreateRequest;
import com.example.account.receivable.GL.Dto.GlTransactionLineDto;
import com.example.account.receivable.GL.Dto.GlTransactionResponse;
import com.example.account.receivable.GL.Entity.GlTransaction;
import com.example.account.receivable.GL.Entity.GlTransactionLine;
import com.example.account.receivable.GL.Enum.GlEntryType;
import com.example.account.receivable.GL.Enum.GlReferenceType;
import com.example.account.receivable.GL.Enum.GlTransactionStatus;
import com.example.account.receivable.GL.Repository.GlTransactionRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class GlTransactionService {

    private final CompanyRepository companyRepository;
    // private final UsersRepository usersRepository;
    // private final ArCodeRepository arCodeRepository;
    // private final ArGlMappingRepository arGlMappingRepository;
    private final GlTransactionRepository glTransactionRepository;

@Transactional
public GlTransactionResponse createTransaction(
    Long companyId,
    GlTransactionCreateRequest request
) {

    Company company = companyRepository.findById(companyId)
        .orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

    GlEntryType entryType =
        request.getReferenceType() == GlReferenceType.INVOICE
            ? GlEntryType.DEBIT
            : GlEntryType.CREDIT;

    GlTransaction transaction = GlTransaction.builder()
        .company(company)
        .referenceType(request.getReferenceType())
        .referenceId(request.getReferenceId())
        .referenceNumber(request.getReferenceNumber())
        .transactionDate(
            request.getTransactionDate() != null
                ? request.getTransactionDate()
                : LocalDate.now()
        )
        .amount(request.getAmount())
        .description(request.getDescription())
        .status(GlTransactionStatus.POSTED)
        .build();

    GlTransactionLine line = GlTransactionLine.builder()
        .transaction(transaction)
        .entryType(entryType)
        .amount(request.getAmount())
        .narration(request.getDescription())
        .build();

    transaction.setLines(List.of(line));

    return toResponse(glTransactionRepository.save(transaction));
}

    @Transactional(readOnly = true)
    public Page<GlTransactionResponse> getCompanyTransactions(
        Long companyId,
        int page,
        int size,
        GlReferenceType referenceType,
        LocalDate fromDate,
        LocalDate toDate
    ) {
        companyRepository.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        Pageable pageable = PageRequest.of(page, size);

        return glTransactionRepository
            .findCompanyTransactions(companyId, referenceType, fromDate, toDate, pageable)
            .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GlTransactionResponse getTransaction(Long transactionId) {
        GlTransaction transaction = glTransactionRepository.findById(transactionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GL transaction not found"));

        return toResponse(transaction);
    }

    private GlTransactionResponse toResponse(GlTransaction transaction) {
        List<GlTransactionLineDto> lineDtos =
            transaction.getLines()
                .stream()
                .map(line -> GlTransactionLineDto.builder()
                    // .glCodeId(line.getGlCode().getId())
                    // .glCode(line.getGlCode().getGlCode())
                    // .glCodeDescription(line.getGlCode().getDescription())
                    .entryType(line.getEntryType())
                    .amount(line.getAmount())
                    .build())
                .collect(Collectors.toList());

        return GlTransactionResponse.builder()
            .id(transaction.getId())
            .companyId(transaction.getCompany().getId())
            .companyName(transaction.getCompany().getLegalName())
            // .arCodeId(transaction.getArCode().getId())
            // .arCode(transaction.getArCode().getCode())
            .referenceType(transaction.getReferenceType())
            .referenceId(transaction.getReferenceId())
            .referenceNumber(transaction.getReferenceNumber())
            .transactionDate(transaction.getTransactionDate())
            .amount(transaction.getAmount())
            .status(transaction.getStatus())
            .description(transaction.getDescription())
            // .createdBy(transaction.getCreatedBy() != null ? transaction.getCreatedBy().getId() : null)
            .createdAt(transaction.getCreatedAt())
            .lines(lineDtos)
            .build();
    }
}
