package com.example.account.receivable.BankAccount_GlMapping.Service;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingRequest;
import com.example.account.receivable.BankAccount_GlMapping.Entity.BankAccountGlMapping;
import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Repository.BankAccountGlMappingRepository;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Entity.CompanyBankAccount;
import com.example.account.receivable.Company.Repository.CompanyBankAccountRepository;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.GLCodes.Entity.GlCode;
import com.example.account.receivable.GLCodes.Repository.GlCodeRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankAccountGlMappingService {

    private final BankAccountGlMappingRepository mappingRepo;
    private final CompanyBankAccountRepository bankRepo;
    private final GlCodeRepository glCodeRepo;
    private final CompanyRepository companyRepo;



    @Transactional
    public BankAccountGlMapping createMapping(
            Long companyId,
            BankAccountGlMappingRequest request
    ) {

        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"));

        CompanyBankAccount bankAccount =
                bankRepo.findById(request.getBankAccountId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Bank account not found"));

        GlCode glCode =
                glCodeRepo.findByIdAndCompanyId(
                        request.getGlCodeId(), companyId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST, "Invalid GL code"));

        // check if ACTIVE mapping already exists
        if (mappingRepo.findByBankAccount_IdAndStatus(
                bankAccount.getId(), MappingStatus.ACTIVE).isPresent()) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Bank account is already mapped to a GL code"
            );
        }

        BankAccountGlMapping mapping = BankAccountGlMapping.builder()
                .company(company)
                .bankAccount(bankAccount)
                .glCode(glCode)
                .status(
                    request.getStatus() != null
                        ? request.getStatus()
                        : MappingStatus.ACTIVE
                )
                .effectiveFrom(
                    request.getEffectiveFrom() != null
                        ? request.getEffectiveFrom()
                        : LocalDate.now()
                )
                .build();

        return mappingRepo.save(mapping);
    }




    //Update Mapping
    @Transactional
    public BankAccountGlMapping updateMapping(
            Long companyId,
            Long mappingId,
            BankAccountGlMappingRequest request
    ) {

        BankAccountGlMapping mapping =
                mappingRepo.findByIdAndCompany_Id(mappingId, companyId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Mapping not found"));

        if (request.getGlCodeId() != null) {
            GlCode glCode =
                    glCodeRepo.findByIdAndCompanyId(
                            request.getGlCodeId(), companyId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.BAD_REQUEST, "Invalid GL code"));
            mapping.setGlCode(glCode);
        }

        if (request.getStatus() != null) {

            if (request.getStatus() == MappingStatus.INACTIVE) {
                mapping.setStatus(MappingStatus.INACTIVE);
                mapping.setEffectiveTo(LocalDate.now());
            }

            if (request.getStatus() == MappingStatus.ACTIVE) {

                // ensure only ONE ACTIVE mapping
                mappingRepo.findByBankAccount_IdAndStatus(
                        mapping.getBankAccount().getId(),
                        MappingStatus.ACTIVE
                ).ifPresent(existing -> {
                    if (!existing.getId().equals(mapping.getId())) {
                        existing.setStatus(MappingStatus.INACTIVE);
                        existing.setEffectiveTo(LocalDate.now().minusDays(1));
                        mappingRepo.save(existing);
                    }
                });

                mapping.setStatus(MappingStatus.ACTIVE);
                mapping.setEffectiveTo(null);
            }
        }

        if (request.getEffectiveFrom() != null) {
            mapping.setEffectiveFrom(request.getEffectiveFrom());
        }

        return mappingRepo.save(mapping);
    }
}

