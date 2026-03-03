package com.example.account.receivable.BankAccount_GlMapping.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingDetailDto;
import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountListDto;
import com.example.account.receivable.BankAccount_GlMapping.Entity.BankAccountGlMapping;
import com.example.account.receivable.BankAccount_GlMapping.Enum.BankGlMappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Repository.BankAccountGlMappingRepository;
import com.example.account.receivable.Company.Entity.CompanyBankAccount;
import com.example.account.receivable.Company.Repository.CompanyBankAccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyBankAccountService {

    private final CompanyBankAccountRepository bankRepo;
    private final BankAccountGlMappingRepository mappingRepo;


    //Get Company Bank Accounts
    public List<BankAccountListDto> getCompanyBankAccounts(Long companyId) {

        List<CompanyBankAccount> accounts =
                bankRepo.findByCompanyId(companyId);


        return accounts.stream().map(acc -> {

            boolean configured =
                mappingRepo.findByBankAccount_IdAndStatus(
                    acc.getId(),
                    MappingStatus.ACTIVE
                ).isPresent();

            return BankAccountListDto.builder()
                .bankAccountId(acc.getId())
                .bankName(acc.getBankName())
                .accountNumber(acc.getAccountNumber())
                .address(acc.getAddress())
                .branch(acc.getBranch())
                .currency(acc.getCurrency())
                .isDefault(acc.getIsDefault())
                .mappingStatus(
                    configured
                        ? BankGlMappingStatus.CONFIGURED
                        : BankGlMappingStatus.MISSING
                )
                .build();
        }).toList();
    }



    //Get Mapping Detail
    public BankAccountGlMappingDetailDto getMappingDetail(Long bankAccountId) {


        CompanyBankAccount account = bankRepo.findById(bankAccountId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "account not found"));


        BankAccountGlMapping mapping =
            mappingRepo.findByBankAccount_IdAndStatus(
                bankAccountId,
                MappingStatus.ACTIVE
            ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No active GL mapping found"
            ));

        return BankAccountGlMappingDetailDto.builder()
            .mappingId(mapping.getId())
            .bankAccountId(bankAccountId)
            .bankName(account.getBankName())
            .bankNumber(account.getAccountNumber())
            .glCodeId(mapping.getGlCode().getId())
            .glCode(mapping.getGlCode().getGlCode())
            .glDescription(mapping.getGlCode().getDescription())
            .status(mapping.getStatus())
            .build();
    }
}

