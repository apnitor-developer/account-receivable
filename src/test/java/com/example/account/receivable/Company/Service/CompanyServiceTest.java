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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.EmailTemplateService;
import com.example.account.receivable.Company.Dto.CompanyProfileRequest;
import com.example.account.receivable.Company.Dto.CompanyUserRequest;
import com.example.account.receivable.Company.Dto.OpeningBalanceFileResponse;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Entity.CompanyOpeningBalanceFile;
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
    void createCompanyUser_persistsUserRolesWithoutSendingEmail() {
        Company company = Company.builder().id(42L).legalName("Globex").build();
        company.setUserCompanies(new ArrayList<>());
        when(companyRepository.findByIdAndDeletedFalse(42L)).thenReturn(Optional.of(company));
        when(usersRepository.findByEmailAndDeletedFalse("new@acme.com"))
                .thenReturn(Optional.empty());

        Users savedUser = Users.builder()
                .id(80L)
                .email("new@acme.com")
                .status(UserStatus.PENDING_APPROVAL)
                .build();
        when(usersRepository.save(any(Users.class))).thenReturn(savedUser);
        when(userCompanyRepository.save(any(UserCompany.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role role = Role.builder().id(7L).name("ADMIN").build();
        when(roleRepository.findById(7L)).thenReturn(Optional.of(role));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompanyUserRequest dto = new CompanyUserRequest();
        dto.setEmail("new@acme.com");
        dto.setFirstName(null);
        dto.setLastName("Smith");
        dto.setRoleIds(List.of(7L));

        Users result = companyService.createCompanyUser(42L, dto);

        assertEquals(savedUser, result);
        assertEquals(UserStatus.PENDING_APPROVAL, result.getStatus());
        verify(userRoleRepository).save(any(UserRole.class));
        verifyNoInteractions(emailTemplateService, emailService);
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

    @Test
    void saveOpeningBalanceFile_whenNonCsv_throwsIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.txt",
                "text/plain",
                "a,b,c".getBytes()
        );

        assertThrows(IllegalArgumentException.class,
                () -> companyService.saveOpeningBalanceFile(9L, file));
        verifyNoInteractions(companyRepository, openingBalanceFileRepository);
    }

    @Test
    void saveOpeningBalanceFile_replacesExistingAndSavesFile() throws Exception {
        Company company = Company.builder().id(15L).build();
        when(companyRepository.findByIdAndDeletedFalse(15L)).thenReturn(Optional.of(company));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "opening.csv",
                "text/csv",
                "col1,col2\n1,2".getBytes()
        );

        byte[] fileBytes = file.getBytes();

        when(openingBalanceFileRepository.save(any(CompanyOpeningBalanceFile.class)))
                .thenAnswer(invocation -> {
                    CompanyOpeningBalanceFile entity = invocation.getArgument(0);
                    entity.setId(200L);
                    return entity;
                });

        OpeningBalanceFileResponse response = companyService.saveOpeningBalanceFile(15L, file);

        assertEquals(200L, response.getId());
        assertEquals("opening.csv", response.getFileName());
        verify(openingBalanceFileRepository).deleteByCompany_Id(15L);
        verify(openingBalanceFileRepository).save(argThat(entity ->
                entity.getCompany().equals(company)
                        && "opening.csv".equals(entity.getFileName())
                        && "text/csv".equals(entity.getContentType())
                        && Long.valueOf(file.getSize()).equals(entity.getFileSize())
                        && Arrays.equals(entity.getData(), fileBytes)
        ));
    }

    @Test
    void approveUser_whenNotPending_throwsBadRequest() {
        Users user = Users.builder()
                .id(1L)
                .email("user@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        when(usersRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.approveUser(1L, 2L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(usersRepository, never()).save(any());
        verifyNoInteractions(emailTemplateService, emailService);
    }

    @Test
    void approveUser_convertsPendingUserAndSendsInvite() {
        Users user = Users.builder()
                .id(2L)
                .email("pending@example.com")
                .firstName("Pending")
                .status(UserStatus.PENDING_APPROVAL)
                .build();

        Company company = Company.builder().id(99L).legalName("Globex").build();

        when(usersRepository.findById(2L)).thenReturn(Optional.of(user));
        when(usersRepository.save(user)).thenReturn(user);
        when(companyRepository.findByIdAndDeletedFalse(99L)).thenReturn(Optional.of(company));
        when(emailTemplateService.buildInviteEmail(eq("Pending"), eq("Globex"), any()))
                .thenReturn("<invite/>");

        companyService.approveUser(2L, 99L);

        assertEquals(UserStatus.INVITED, user.getStatus());
        verify(emailService).sendWithAttachment(
                eq("pending@example.com"),
                eq("You're invited to join"),
                eq("<invite/>"),
                eq(null)
        );
    }

    @Test
    void validateInvite_whenUserMissing_throwsNotFound() {
        when(usersRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.validateInvite("ghost@example.com"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void validateInvite_whenStatusNotInvited_throwsBadRequest() {
        Users user = Users.builder()
                .id(3L)
                .email("notinvited@example.com")
                .status(UserStatus.ACTIVE)
                .build();
        when(usersRepository.findByEmail("notinvited@example.com")).thenReturn(Optional.of(user));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.validateInvite("notinvited@example.com"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void getcompanyUsers_whenEmpty_throwsNotFound() {
        when(usersRepository.findByCompany_Id(33L)).thenReturn(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.getcompanyUsers(33L));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getcompanyUsers_returnsUsers() {
        Users user = Users.builder().id(10L).build();
        when(usersRepository.findByCompany_Id(44L)).thenReturn(List.of(user));

        List<Users> result = companyService.getcompanyUsers(44L);

        assertEquals(1, result.size());
        assertEquals(user, result.get(0));
    }

    @Test
    void assignRoleToUser_whenDuplicate_throwsBadRequest() {
        Users user = Users.builder().id(100L).build();
        Role role = Role.builder().id(200L).build();

        when(usersRepository.findById(100L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(200L)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserAndRole(user, role)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> companyService.assignRoleToUser(100L, 200L));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(userRoleRepository, never()).save(any());
    }

    @Test
    void assignRoleToUser_persistsRoleWhenNotAssigned() {
        Users user = Users.builder().id(101L).build();
        Role role = Role.builder().id(201L).build();

        when(usersRepository.findById(101L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(201L)).thenReturn(Optional.of(role));
        when(userRoleRepository.existsByUserAndRole(user, role)).thenReturn(false);
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));

        companyService.assignRoleToUser(101L, 201L);

        verify(userRoleRepository).save(argThat(ur -> ur.getUser().equals(user) && ur.getRole().equals(role)));
    }
}
