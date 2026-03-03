package com.example.account.receivable.BankAccount_GlMapping.Controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingDetailDto;
import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountListDto;
import com.example.account.receivable.BankAccount_GlMapping.Enum.BankGlMappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Service.CompanyBankAccountService;
import com.example.account.receivable.Common.Enum.CurrencyEnum;
import com.example.account.receivable.ControllerTestSecurityConfig;

@WebMvcTest(CompanyBankAccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerTestSecurityConfig.class, CompanyBankAccountControllerTest.TestConfig.class})
class CompanyBankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CompanyBankAccountService service;

    @Test
    void getAccounts_returnsCompanyBankAccounts() throws Exception {
        BankAccountListDto dto = BankAccountListDto.builder()
                .bankAccountId(55L)
                .bankName("Cash Bank")
                .accountNumber("123456")
                .currency(CurrencyEnum.USD)
                .isDefault(true)
                .mappingStatus(BankGlMappingStatus.CONFIGURED)
                .build();

        when(service.getCompanyBankAccounts(4L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/companies/{companyId}/bank-accounts", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data[0].bankAccountId").value(55))
                .andExpect(jsonPath("$.data[0].mappingStatus").value("CONFIGURED"));

        verify(service).getCompanyBankAccounts(4L);
    }

    @Test
    void getMapping_returnsMappingDetails() throws Exception {
        BankAccountGlMappingDetailDto detailDto = BankAccountGlMappingDetailDto.builder()
                .mappingId(77L)
                .bankAccountId(20L)
                .bankName("Cash Bank")
                .bankNumber("123456")
                .glCodeId(88L)
                .glCode("1100")
                .glDescription("Cash On Hand")
                .status(MappingStatus.ACTIVE)
                .build();

        when(service.getMappingDetail(20L)).thenReturn(detailDto);

        mockMvc.perform(get("/api/companies/{bankAccountId}/gl-mapping", 20L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mappingId").value(77))
                .andExpect(jsonPath("$.data.glCode").value("1100"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(service).getMappingDetail(eq(20L));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CompanyBankAccountService companyBankAccountService() {
            return Mockito.mock(CompanyBankAccountService.class);
        }
    }
}
