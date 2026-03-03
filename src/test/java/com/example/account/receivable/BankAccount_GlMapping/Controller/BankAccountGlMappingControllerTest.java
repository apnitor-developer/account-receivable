package com.example.account.receivable.BankAccount_GlMapping.Controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.BankAccount_GlMapping.DTO.BankAccountGlMappingRequest;
import com.example.account.receivable.BankAccount_GlMapping.Entity.BankAccountGlMapping;
import com.example.account.receivable.BankAccount_GlMapping.Enum.MappingStatus;
import com.example.account.receivable.BankAccount_GlMapping.Service.BankAccountGlMappingService;
import com.example.account.receivable.ControllerTestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BankAccountGlMappingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ControllerTestSecurityConfig.class, BankAccountGlMappingControllerTest.TestConfig.class})
class BankAccountGlMappingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankAccountGlMappingService service;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Test
    void createMapping_returnsCreatedEntity() throws Exception {
        BankAccountGlMappingRequest request = new BankAccountGlMappingRequest();
        request.setBankAccountId(15L);
        request.setGlCodeId(25L);
        request.setStatus(MappingStatus.ACTIVE);

        BankAccountGlMapping mapping = BankAccountGlMapping.builder()
                .id(100L)
                .status(MappingStatus.ACTIVE)
                .build();

        when(service.createMapping(eq(9L), any(BankAccountGlMappingRequest.class))).thenReturn(mapping);

        mockMvc.perform(post("/api/bank-gl-mappings/company/{companyId}", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(service).createMapping(eq(9L), any(BankAccountGlMappingRequest.class));
    }

    @Test
    void updateMapping_returnsUpdatedEntity() throws Exception {
        BankAccountGlMappingRequest request = new BankAccountGlMappingRequest();
        request.setGlCodeId(30L);
        request.setStatus(MappingStatus.INACTIVE);

        BankAccountGlMapping mapping = BankAccountGlMapping.builder()
                .id(200L)
                .status(MappingStatus.INACTIVE)
                .build();

        when(service.updateMapping(eq(3L), eq(7L), any(BankAccountGlMappingRequest.class)))
                .thenReturn(mapping);

        mockMvc.perform(put("/api/bank-gl-mappings/company/{companyId}/{mappingId}", 3L, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(200))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        verify(service).updateMapping(eq(3L), eq(7L), any(BankAccountGlMappingRequest.class));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        BankAccountGlMappingService bankAccountGlMappingService() {
            return Mockito.mock(BankAccountGlMappingService.class);
        }
    }
}
