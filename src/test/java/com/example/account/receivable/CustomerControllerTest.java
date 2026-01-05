package com.example.account.receivable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.Customer.Controller.CustomerController;
import com.example.account.receivable.Customer.Dto.CompanyResponseDto.CustomerResponseDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerCsv;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerDunningCreditSettingsDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerEftDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerFullRequestDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerStatementDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerVatDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerFullUpdateDTO;
import com.example.account.receivable.Customer.Entity.CashApplication;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Entity.CustomerAddress;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Entity.CustomerEFT;
import com.example.account.receivable.Customer.Entity.CustomerStatement;
import com.example.account.receivable.Customer.Entity.CustomerVAT;
import com.example.account.receivable.Customer.Service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({CustomerControllerTest.TestConfig.class, ControllerTestSecurityConfig.class})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CustomerService customerService;

    private Customer sampleCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCustomerName("Acme Customer");
        customer.setEmail("customer@example.com");
        customer.setCustomerType("BUSINESS");
        return customer;
    }

    @Test
    void createCustomer_returnsEntity() throws Exception {
        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerName("Acme Customer");
        dto.setEmail("customer@example.com");
        dto.setCustomerType("BUSINESS");

        when(customerService.createCustomer(eq(3L), eq(5L), any(CustomerDTO.class))).thenReturn(sampleCustomer());

        mockMvc.perform(
                post("/customer/{userId}/{companyId}", 3L, 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customerName").value("Acme Customer"));

        verify(customerService).createCustomer(eq(3L), eq(5L), any(CustomerDTO.class));
    }

    @Test
    void saveAddress_persistsAddress() throws Exception {
        CustomerAddress address = new CustomerAddress();
        address.setAddressLine1("123 Main");
        address.setCity("Austin");

        when(customerService.saveCustomerAddress(eq(2L), any(CustomerAddress.class))).thenReturn(address);

        mockMvc.perform(
                post("/customer/{customerId}/address", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(address))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.city").value("Austin"));

        verify(customerService).saveCustomerAddress(eq(2L), any(CustomerAddress.class));
    }

    @Test
    void saveCashApplication_recordsConfig() throws Exception {
        CashApplication cashApplication = new CashApplication();
        cashApplication.setApplyPayments(true);

        when(customerService.saveCashApplication(eq(2L), any(CashApplication.class))).thenReturn(cashApplication);

        mockMvc.perform(
                post("/customer/{customerId}/cash-application", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cashApplication))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.applyPayments").value(true));

        verify(customerService).saveCashApplication(eq(2L), any(CashApplication.class));
    }

    @Test
    void saveCustomerStatement_recordsPreferences() throws Exception {
        CustomerStatementDTO request = new CustomerStatementDTO();
        request.setSendStatements(true);
        request.setMinimumAmount(50.0);

        CustomerStatement statement = new CustomerStatement();
        statement.setMinimumAmount(50.0);

        when(customerService.saveCustomerStatement(eq(4L), any(CustomerStatementDTO.class))).thenReturn(statement);

        mockMvc.perform(
                post("/customer/{customerId}/statement", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.minimumAmount").value(50.0));

        verify(customerService).saveCustomerStatement(eq(4L), any(CustomerStatementDTO.class));
    }

    @Test
    void saveEft_detailsRecorded() throws Exception {
        CustomerEftDTO dto = new CustomerEftDTO();
        dto.setBankName("Bank");
        dto.setIbanAccountNumber("IBAN123");

        CustomerEFT eft = new CustomerEFT();
        eft.setBankName("Bank");

        when(customerService.saveCustomerEft(eq(3L), any(CustomerEftDTO.class))).thenReturn(eft);

        mockMvc.perform(
                post("/customer/{customerId}/eft", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bankName").value("Bank"));

        verify(customerService).saveCustomerEft(eq(3L), any(CustomerEftDTO.class));
    }

    @Test
    void saveVat_recordsVatDetails() throws Exception {
        CustomerVatDTO dto = new CustomerVatDTO();
        dto.setTaxIdentificationNumber("TIN123");
        dto.setTaxAgencyName("Agency");

        CustomerVAT vat = new CustomerVAT();
        vat.setTaxAgencyName("Agency");

        when(customerService.saveCustomerVat(eq(6L), any(CustomerVatDTO.class))).thenReturn(vat);

        mockMvc.perform(
                post("/customer/{customerId}/vat", 6L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.taxAgencyName").value("Agency"));

        verify(customerService).saveCustomerVat(eq(6L), any(CustomerVatDTO.class));
    }

    @Test
    void saveDunningCredit_recordsSettings() throws Exception {
        CustomerDunningCreditSettingsDTO dto = new CustomerDunningCreditSettingsDTO();
        dto.setCreditLimit(5000.0);
        dto.setPlaceOnCreditHold(true);

        CustomerDunningCreditSettings settings = new CustomerDunningCreditSettings();
        settings.setCreditLimit(5000.0);

        when(customerService.saveCustomerDunningCreditSettings(eq(5L), any(CustomerDunningCreditSettingsDTO.class)))
                .thenReturn(settings);

        mockMvc.perform(
                post("/customer/{customerId}/dunning-credit", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.creditLimit").value(5000.0));

        verify(customerService).saveCustomerDunningCreditSettings(eq(5L), any(CustomerDunningCreditSettingsDTO.class));
    }

    @Test
    void getSingleCustomer_returnsProjection() throws Exception {
        CustomerResponseDTO response = new CustomerResponseDTO();
        response.setId(7L);
        response.setCustomerName("Acme Customer");
        response.setEmail("customer@example.com");

        when(customerService.getSingleCustomer(7L)).thenReturn(response);

        mockMvc.perform(get("/customer/{id}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("customer@example.com"));

        verify(customerService).getSingleCustomer(7L);
    }

    @Test
    void getAllCustomers_returnsPage() throws Exception {
        Page<Customer> page = new PageImpl<>(List.of(sampleCustomer()));
        when(customerService.getAllCustomers(0, 10)).thenReturn(page);

        mockMvc.perform(get("/customer").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].customerName").value("Acme Customer"));

        verify(customerService).getAllCustomers(0, 10);
    }

    @Test
    void getCustomersByCompany_returnsPagedList() throws Exception {
        Page<Customer> page = new PageImpl<>(List.of(sampleCustomer()));
        when(customerService.getCustomersByCompanyId(4L, 1, 5)).thenReturn(page);

        mockMvc.perform(get("/customer/company/{companyId}", 4L).param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("customer@example.com"));

        verify(customerService).getCustomersByCompanyId(4L, 1, 5);
    }

    @Test
    void softDeleteCustomer_marksDeleted() throws Exception {
        Customer deleted = sampleCustomer();
        deleted.setDeleted(true);
        when(customerService.softDeleteCustomer(8L)).thenReturn(deleted);

        mockMvc.perform(delete("/customer/{customerId}", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted Successfully"));

        verify(customerService).softDeleteCustomer(8L);
    }

    @Test
    void createCompleteCustomer_createsAllSections() throws Exception {
        CustomerFullRequestDTO request = new CustomerFullRequestDTO();
        request.setCustomerName("Full Customer");
        request.setEmail("full@example.com");

        when(customerService.createCompleteCustomer(any(CustomerFullRequestDTO.class))).thenReturn(sampleCustomer());

        mockMvc.perform(
                post("/customer/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customerName").value("Acme Customer"));

        verify(customerService).createCompleteCustomer(any(CustomerFullRequestDTO.class));
    }

    @Test
    void updateCustomer_updatesFields() throws Exception {
        CustomerFullUpdateDTO updateDto = new CustomerFullUpdateDTO();
        updateDto.setCustomerName("New Name");

        Customer updated = sampleCustomer();
        updated.setCustomerName("New Name");

        when(customerService.updateCustomer(eq(9L), any(CustomerFullUpdateDTO.class))).thenReturn(updated);

        mockMvc.perform(
                patch("/customer/{customerId}", 9L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerName").value("New Name"));

        verify(customerService).updateCustomer(eq(9L), any(CustomerFullUpdateDTO.class));
    }

    @Test
    void importCustomersCsv_processesFile() throws Exception {
        CustomerCsv response = CustomerCsv.builder()
                .totalRows(2)
                .successCount(2)
                .failureCount(0)
                .build();

        MockMultipartFile file = new MockMultipartFile("file", "customers.csv", "text/csv", "data".getBytes());

        when(customerService.importCustomersFromCsv(eq(4L), any())).thenReturn(response);

        mockMvc.perform(multipart("/customer/import-csv").file(file).param("companyId", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalRows").value(2));

        verify(customerService).importCustomersFromCsv(eq(4L), any());
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CustomerService customerService() {
            return Mockito.mock(CustomerService.class);
        }
    }
}
