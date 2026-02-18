package com.example.account.receivable;

import static org.hamcrest.Matchers.containsString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
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

import com.example.account.receivable.Common.Premission.Permission;
import com.example.account.receivable.Company.Controller.CompanyController;
import com.example.account.receivable.Company.Controller.PermissionController;
import com.example.account.receivable.Company.Dto.BankAccountRequest;
import com.example.account.receivable.Company.Dto.CompanyContactAddressRequest;
import com.example.account.receivable.Company.Dto.CompanyPatchRequest;
import com.example.account.receivable.Company.Dto.CompanyProfileRequest;
import com.example.account.receivable.Company.Dto.CompanyUserRequest;
import com.example.account.receivable.Company.Dto.FinancialSettingsRequest;
import com.example.account.receivable.Company.Dto.OpeningBalanceFileResponse;
import com.example.account.receivable.Company.Dto.RoleDto;
import com.example.account.receivable.Company.Dto.RoleResponse;
import com.example.account.receivable.Company.Dto.SetPasswordDto;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Entity.CompanyAddress;
import com.example.account.receivable.Company.Entity.CompanyBankAccount;
import com.example.account.receivable.Company.Service.CompanyService;
import com.example.account.receivable.User.Enum.UserStatus;
import com.example.account.receivable.User.controller.RoleController;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest({CompanyController.class, PermissionController.class, RoleController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({CompanyAndRoleControllersTest.TestConfig.class, ControllerTestSecurityConfig.class})
class CompanyAndRoleControllersTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private CompanyService companyService;

    @Autowired
    private RoleService roleService;

    private Company sampleCompany() {
        return Company.builder()
                .id(1L)
                .legalName("Acme Corp")
                .tradeName("Acme")
                .companyCode("ACME")
                .country("US")
                .baseCurrency("USD")
                .timeZone("UTC")
                .build();
    }

    private Users sampleUser() {
        return Users.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane@example.com")
                .status(UserStatus.INVITED)
                .build();
    }

    @Test
    void patchCompany_updatesCompanyFields() throws Exception {
        CompanyPatchRequest request = new CompanyPatchRequest();
        request.setLegalName("Updated Co");

        Company updated = sampleCompany();
        updated.setLegalName("Updated Co");

        when(companyService.patchCompany(eq(1L), any(CompanyPatchRequest.class))).thenReturn(updated);

        mockMvc.perform(
                patch("/api/companies/{id}/update", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.legalName").value("Updated Co"));

        verify(companyService).patchCompany(eq(1L), any(CompanyPatchRequest.class));
    }

    @Test
    void inviteUsers_createsCompanyUser() throws Exception {
        CompanyUserRequest request = new CompanyUserRequest();
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setEmail("jane@example.com");

        when(companyService.createCompanyUser(eq(5L), any(CompanyUserRequest.class))).thenReturn(sampleUser());

        mockMvc.perform(
                post("/api/companies/{companyId}/users", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("jane@example.com"));

        verify(companyService).createCompanyUser(eq(5L), any(CompanyUserRequest.class));
    }

    @Test
    void acceptInvite_redirectsToFrontend() throws Exception {
        doNothing().when(companyService).validateInvite("test@example.com");

        mockMvc.perform(get("/api/companies/company/users/accept").param("email", "test@example.com"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", containsString("test@example.com")));

        verify(companyService).validateInvite("test@example.com");
    }

    @Test
    void setPassword_confirmsInvitation() throws Exception {
        SetPasswordDto dto = new SetPasswordDto();
        dto.setEmail("invitee@example.com");
        dto.setPassword("secret123");

        doNothing().when(companyService).acceptInvitation(dto.getEmail(), dto.getPassword());

        mockMvc.perform(
                post("/api/companies/user/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password set successfully"));

        verify(companyService).acceptInvitation(dto.getEmail(), dto.getPassword());
    }

    @Test
    void getCompanyUsers_returnsUserList() throws Exception {
        when(companyService.getcompanyUsers(9L)).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/companies/users/{companyId}", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value("jane@example.com"));

        verify(companyService).getcompanyUsers(9L);
    }

    @Test
    void uploadOpeningBalanceFile_savesCsv() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "balances.csv",
                "text/csv",
                "header\nrow".getBytes()
        );
        OpeningBalanceFileResponse response = OpeningBalanceFileResponse.builder()
                .id(3L)
                .fileName("balances.csv")
                .build();
        when(companyService.saveOpeningBalanceFile(eq(2L), any())).thenReturn(response);

        mockMvc.perform(multipart("/api/companies/{id}/opening-balance-file", 2L).file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("balances.csv"));

        verify(companyService).saveOpeningBalanceFile(eq(2L), any());
    }

    @Test
    void createCompany_createsStepOne() throws Exception {
        CompanyProfileRequest request = new CompanyProfileRequest();
        request.setLegalName("Acme New");
        request.setCompanyCode("ACM1");

        Company company = sampleCompany();
        company.setLegalName("Acme New");

        when(companyService.createCompanyStep1(eq(12L), any(CompanyProfileRequest.class))).thenReturn(company);

        mockMvc.perform(
                post("/api/companies/{userId}", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.legalName").value("Acme New"));

        verify(companyService).createCompanyStep1(eq(12L), any(CompanyProfileRequest.class));
    }

    @Test
    void saveContactAndAddress_persistsData() throws Exception {
        CompanyContactAddressRequest request = new CompanyContactAddressRequest();
        request.setCity("New York");
        request.setPrimaryContactEmail("contact@example.com");

        CompanyAddress address = CompanyAddress.builder()
                .id(1L)
                .city("New York")
                .primaryContactEmail("contact@example.com")
                .build();

        when(companyService.createCompanyAddress(eq(7L), any(CompanyContactAddressRequest.class))).thenReturn(address);

        mockMvc.perform(
                post("/api/companies/{id}/company-address", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.city").value("New York"));

        verify(companyService).createCompanyAddress(eq(7L), any(CompanyContactAddressRequest.class));
    }

    @Test
    void saveFinancialSettings_updatesConfig() throws Exception {
        FinancialSettingsRequest request = new FinancialSettingsRequest();
        request.setDefaultCreditLimit(BigDecimal.valueOf(10000));

        Company company = sampleCompany();

        doNothing().when(companyService).upsertFinancialSettings(eq(3L), any(FinancialSettingsRequest.class));
        when(companyService.getCompanyDetails(3L)).thenReturn(company);

        mockMvc.perform(
                post("/api/companies/{id}/financial-settings", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.legalName").value("Acme Corp"));

        verify(companyService).upsertFinancialSettings(eq(3L), any(FinancialSettingsRequest.class));
    }

    @Test
    void saveBanking_createsBankAccount() throws Exception {
        BankAccountRequest request = new BankAccountRequest();
        request.setBankName("Bank");
        request.setAccountNumber("123");

        CompanyBankAccount account = CompanyBankAccount.builder()
                .id(10L)
                .bankName("Bank")
                .accountNumber("123")
                .build();

        when(companyService.createBankAccount(eq(4L), any(BankAccountRequest.class))).thenReturn(account);

        mockMvc.perform(
                post("/api/companies/{id}/banking", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bankName").value("Bank"))
                .andExpect(jsonPath("$.data.accountNumber").value("123"));

        verify(companyService).createBankAccount(eq(4L), any(BankAccountRequest.class));
    }

    @Test
    void getCompany_returnsDetails() throws Exception {
        when(companyService.getCompanyDetails(8L)).thenReturn(sampleCompany());

        mockMvc.perform(get("/api/companies/{id}", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.legalName").value("Acme Corp"));

        verify(companyService).getCompanyDetails(8L);
    }

    @Test
    void getAllCompanies_returnsPage() throws Exception {
        Page<Company> page = new PageImpl<>(List.of(sampleCompany()));
        when(companyService.getAllCompanies(6L, 0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/companies/user/{userId}", 6L).param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].companyCode").value("ACME"));

        verify(companyService).getAllCompanies(6L, 0, 10);
    }

    @Test
    void deleteCompany_softDeletesEntity() throws Exception {
        doNothing().when(companyService).deleteCompany(13L);

        mockMvc.perform(delete("/api/companies/{id}", 13L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Company deleted successfully"));

        verify(companyService).deleteCompany(13L);
    }

    @Test
    void getAllPermissions_returnsEnumNames() throws Exception {
        mockMvc.perform(get("/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(Permission.values().length));
    }

    @Test
    void createRole_returnsCreatedRole() throws Exception {
        RoleDto request = new RoleDto();
        request.setName("Manager");
        request.setPermissions(Set.of(Permission.VIEW_DASHBOARD));

        RoleResponse response = new RoleResponse(1L, "Manager", "desc", request.getPermissions());
        when(roleService.createRole(eq(3L), any(RoleDto.class))).thenReturn(response);

        mockMvc.perform(
                post("/api/roles/company/{companyId}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Manager"));

        verify(roleService).createRole(eq(3L), any(RoleDto.class));
    }

    @Test
    void getAllRoles_returnsCompanyRoles() throws Exception {
        RoleResponse response = new RoleResponse(1L, "Reader", "desc", Set.of(Permission.VIEW_DASHBOARD));
        when(roleService.getAllRoles(2L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/roles/company/{companyId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Reader"));

        verify(roleService).getAllRoles(2L);
    }

    @Test
    void getRole_returnsSingleRole() throws Exception {
        RoleResponse response = new RoleResponse(9L, "Owner", "desc", Set.of(Permission.VIEW_DASHBOARD));
        when(roleService.getRole(9L)).thenReturn(response);

        mockMvc.perform(get("/api/roles/{id}", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(9));

        verify(roleService).getRole(9L);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        CompanyService companyService() {
            return Mockito.mock(CompanyService.class);
        }

        @Bean
        RoleService roleService() {
            return Mockito.mock(RoleService.class);
        }
    }
}
