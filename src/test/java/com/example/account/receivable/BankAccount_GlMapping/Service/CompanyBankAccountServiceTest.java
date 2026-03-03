package com.example.account.receivable.BankAccount_GlMapping.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingDetailDto;
import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountListDto;
import com.example.account.receivable.BankAccount_GlMapping.Entity.BankAccountGlMapping;
import com.example.account.receivable.BankAccount_GlMapping.Enum.BankGlMappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Repository.BankAccountGlMappingRepository;
import com.example.account.receivable.Common.Enum.CurrencyEnum;
import com.example.account.receivable.Company.Entity.CompanyBankAccount;
import com.example.account.receivable.Company.Repository.CompanyBankAccountRepository;
import com.example.account.receivable.GLCodes.Entity.GlCode;

@ExtendWith(MockitoExtension.class)
class CompanyBankAccountServiceTest {

    @Mock
    private CompanyBankAccountRepository bankRepo;
    @Mock
    private BankAccountGlMappingRepository mappingRepo;
    @InjectMocks
    private CompanyBankAccountService service;

    @Test
    void getCompanyBankAccounts_marksConfiguredWhenMappingExists() {
        CompanyBankAccount account = CompanyBankAccount.builder()
                .id(11L)
                .bankName("First Bank")
                .accountNumber("000111")
                .address("Main Street")
                .branch("HQ")
                .currency(CurrencyEnum.USD)
                .isDefault(true)
                .build();

        when(bankRepo.findByCompanyId(5L)).thenReturn(List.of(account));
        when(mappingRepo.findByBankAccount_IdAndStatus(11L, MappingStatus.ACTIVE))
                .thenReturn(Optional.of(BankAccountGlMapping.builder().id(22L).build()));

        List<BankAccountListDto> result = service.getCompanyBankAccounts(5L);

        assertEquals(1, result.size());
        BankAccountListDto dto = result.get(0);
        assertEquals(account.getId(), dto.getBankAccountId());
        assertEquals("First Bank", dto.getBankName());
        assertEquals(BankGlMappingStatus.CONFIGURED, dto.getMappingStatus());

        verify(bankRepo).findByCompanyId(5L);
        verify(mappingRepo).findByBankAccount_IdAndStatus(11L, MappingStatus.ACTIVE);
    }

    @Test
    void getCompanyBankAccounts_marksMissingWhenMappingAbsent() {
        CompanyBankAccount account = CompanyBankAccount.builder()
                .id(15L)
                .bankName("Second Bank")
                .accountNumber("000222")
                .currency(CurrencyEnum.GBP)
                .isDefault(false)
                .build();

        when(bankRepo.findByCompanyId(9L)).thenReturn(List.of(account));
        when(mappingRepo.findByBankAccount_IdAndStatus(15L, MappingStatus.ACTIVE))
                .thenReturn(Optional.empty());

        List<BankAccountListDto> result = service.getCompanyBankAccounts(9L);

        assertEquals(1, result.size());
        assertEquals(BankGlMappingStatus.MISSING, result.get(0).getMappingStatus());

        verify(mappingRepo).findByBankAccount_IdAndStatus(15L, MappingStatus.ACTIVE);
    }

    @Test
    void getMappingDetail_returnsCombinedData() {
        CompanyBankAccount account = CompanyBankAccount.builder()
                .id(31L)
                .bankName("Treasury Bank")
                .accountNumber("987654")
                .build();

        GlCode glCode = GlCode.builder()
                .id(41L)
                .glCode("1000")
                .description("Cash")
                .build();

        BankAccountGlMapping mapping = BankAccountGlMapping.builder()
                .id(25L)
                .glCode(glCode)
                .status(MappingStatus.ACTIVE)
                .bankAccount(account)
                .build();

        when(bankRepo.findById(31L)).thenReturn(Optional.of(account));
        when(mappingRepo.findByBankAccount_IdAndStatus(31L, MappingStatus.ACTIVE))
                .thenReturn(Optional.of(mapping));

        BankAccountGlMappingDetailDto dto = service.getMappingDetail(31L);

        assertEquals(25L, dto.getMappingId());
        assertEquals(31L, dto.getBankAccountId());
        assertEquals("Treasury Bank", dto.getBankName());
        assertEquals("987654", dto.getBankNumber());
        assertEquals(41L, dto.getGlCodeId());
        assertEquals("1000", dto.getGlCode());
        assertEquals("Cash", dto.getGlDescription());
        assertEquals(MappingStatus.ACTIVE, dto.getStatus());
    }

    @Test
    void getMappingDetail_whenAccountMissing_throwsNotFound() {
        when(bankRepo.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getMappingDetail(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(mappingRepo, never()).findByBankAccount_IdAndStatus(99L, MappingStatus.ACTIVE);
    }

    @Test
    void getMappingDetail_whenMappingMissing_throwsNotFound() {
        CompanyBankAccount account = CompanyBankAccount.builder().id(77L).build();
        when(bankRepo.findById(77L)).thenReturn(Optional.of(account));
        when(mappingRepo.findByBankAccount_IdAndStatus(77L, MappingStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getMappingDetail(77L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
