package com.example.account.receivable.Company.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.EmailTemplateService;
import com.example.account.receivable.Company.Dto.CompanyProfileRequest;
import com.example.account.receivable.Company.Dto.CompanyUserRequest;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Entity.UserCompany;
import com.example.account.receivable.Company.Repository.CompanyAddressRepository;
import com.example.account.receivable.Company.Repository.CompanyBankAccountRepository;
import com.example.account.receivable.Company.Repository.CompanyFinancialSettingsRepository;
import com.example.account.receivable.Company.Repository.CompanyOpeningBalanceFileRepository;
import com.example.account.receivable.Company.Repository.CompanyPaymentSettingsRepository;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Company.Repository.UserCompanyRepository;
import com.example.account.receivable.User.Enum.UserStatus;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.entity.UserRole;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.RoleRepository;
import com.example.account.receivable.User.repository.UserRoleRepository;
import com.example.account.receivable.User.repository.UsersRepository;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CompanyAddressRepository companyAddressRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyFinancialSettingsRepository financialRepo;
    @Mock
    private CompanyPaymentSettingsRepository paymentRepo;
    @Mock
    private CompanyBankAccountRepository bankAccountRepo;
    @Mock
    private CompanyOpeningBalanceFileRepository openingBalanceFileRepository;
    @Mock
    private EmailTemplateService emailTemplateService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserCompanyRepository userCompanyRepository;
    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    void createCompanyStep1_linksUserToSavedCompany() {
        Users user = Users.builder().id(1L).build();
        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

        Company saved = Company.builder().id(10L).legalName("Acme").build();
        when(companyRepository.save(any(Company.class))).thenReturn(saved);
        when(userCompanyRepository.save(any(UserCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyProfileRequest request = new CompanyProfileRequest();
        request.setLegalName("Acme");
        request.setTradeName("Acme Inc");
        request.setCompanyCode("ACM");
        request.setCountry("US");
        request.setBaseCurrency("USD");
        request.setTimeZone("UTC");

        Company result = companyService.createCompanyStep1(1L, request);

        assertEquals(saved, result);
        verify(userCompanyRepository).save(argThat(uc ->
                uc.getUser().equals(user) && uc.getCompany().equals(saved)));
    }

    @Test
    void createCompanyUser_whenUserAlreadyLinked_throwsConflict() {
        Company company = Company.builder().id(55L).legalName("Globex").build();
        when(companyRepository.findByIdAndDeletedFalse(55L)).thenReturn(Optional.of(company));

        Users existingUser = Users.builder().id(5L).email("member@example.com").build();
        UserCompany link = UserCompany.builder().company(company).user(existingUser).build();
        existingUser.setUserCompanies(new ArrayList<>(List.of(link)));

        when(usersRepository.findByEmailAndDeletedFalse("member@example.com"))
                .thenReturn(Optional.of(existingUser));

        CompanyUserRequest dto = new CompanyUserRequest();
        dto.setEmail("member@example.com");
        dto.setFirstName("Jane");
        dto.setLastName("Doe");
        dto.setRoleIds(List.of(1L));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.createCompanyUser(55L, dto));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(userCompanyRepository, never()).save(any());
    }

    @Test
    void createCompanyUser_persistsUserRolesAndSendsInvite() {
        Company company = Company.builder().id(42L).legalName("Globex").build();
        company.setUserCompanies(new ArrayList<>());
        when(companyRepository.findByIdAndDeletedFalse(42L)).thenReturn(Optional.of(company));
        when(usersRepository.findByEmailAndDeletedFalse("new@acme.com"))
                .thenReturn(Optional.empty());

        Users savedUser = Users.builder()
                .id(80L)
                .email("new@acme.com")
                .status(UserStatus.INVITED)
                .build();
        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);
        when(userCompanyRepository.save(any(UserCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role role = Role.builder().id(7L).name("ADMIN").build();
        when(roleRepository.findById(7L)).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(emailTemplateService.buildInviteEmail(any(), eq(company.getLegalName()), any()))
                .thenReturn("<html></html>");

        CompanyUserRequest dto = new CompanyUserRequest();
        dto.setEmail("new@acme.com");
        dto.setFirstName(null);
        dto.setLastName("Smith");
        dto.setRoleIds(List.of(7L));

        Users result = companyService.createCompanyUser(42L, dto);

        assertEquals(savedUser, result);
        verify(userRoleRepository).save(any(UserRole.class));
        verify(emailService).sendWithAttachment(
                eq("new@acme.com"),
                eq("You're invited to join " + company.getLegalName()),
                eq("<html></html>"),
                eq(null)
        );
    }

    @Test
    void acceptInvitation_whenStatusNotInvited_throwsBadRequest() {
        Users user = Users.builder()
                .id(3L)
                .email("jane@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        when(usersRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.acceptInvitation("jane@example.com", "secret"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(usersRepository, never()).save(any());
    }

    @Test
    void acceptInvitation_encodesPasswordAndActivatesUser() {
        Users user = Users.builder()
                .id(4L)
                .email("invitee@example.com")
                .status(UserStatus.INVITED)
                .build();

        when(usersRepository.findByEmail("invitee@example.com")).thenReturn(Optional.of(user));
        when(usersRepository.save(user)).thenReturn(user);

        companyService.acceptInvitation("invitee@example.com", "StrongPass!23");

        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertNotNull(user.getPassword());
        assertTrue(user.getPassword().startsWith("$2"));
        verify(usersRepository).save(user);
    }
}
