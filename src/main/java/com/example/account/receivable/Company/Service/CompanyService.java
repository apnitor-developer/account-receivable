package com.example.account.receivable.Company.Service;


import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.EmailTemplateService;
import com.example.account.receivable.Company.Dto.*;
import com.example.account.receivable.Company.Entity.*;
import com.example.account.receivable.Company.Repository.*;
import com.example.account.receivable.User.Enum.UserStatus;
import com.example.account.receivable.User.entity.Role;
import com.example.account.receivable.User.entity.UserRole;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.User.repository.RoleRepository;
import com.example.account.receivable.User.repository.UserRoleRepository;
import com.example.account.receivable.User.repository.UsersRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
public class CompanyService {

    private final UsersRepository usersRepository;  
    private final RoleRepository roleRepository;
    private final CompanyAddressRepository companyAddressRepository;   
    // private final CompanyCustomerRepository companyCustomerRepository;
       

    private final CompanyRepository companyRepository;
    private final CompanyFinancialSettingsRepository financialRepo;
    private final CompanyPaymentSettingsRepository paymentRepo;
    private final CompanyBankAccountRepository bankAccountRepo;
    private final CompanyOpeningBalanceFileRepository openingBalanceFileRepository;
    
    private final EmailTemplateService emailTemplateService;
    private final EmailService emailService;
    private final UserCompanyRepository userCompanyRepository;
    private final UserRoleRepository userRoleRepository;


    // STEP 1 – create company
    @Transactional
    public Company createCompanyStep1(Long userId , CompanyProfileRequest request) {

        //Check the User
        Users user = usersRepository.findById(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Company company = Company.builder()
                .legalName(request.getLegalName())
                .tradeName(request.getTradeName())
                .companyCode(request.getCompanyCode())
                .country(request.getCountry())
                .baseCurrency(request.getBaseCurrency())
                .timeZone(request.getTimeZone())
                .build();

        // Save the company
        Company savedCompany = companyRepository.save(company);

        // Create the UserCompany relationship if necessary (for many-to-many)
        UserCompany userCompany = UserCompany.builder()
                .user(user)
                .company(savedCompany)
                .build();

        // Save the UserCompany join table
        userCompanyRepository.save(userCompany);

        return savedCompany;
    }

    //Create Company Address
    public CompanyAddress createCompanyAddress(Long companyId , CompanyContactAddressRequest dto){

            Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Company not found"
            ));

            CompanyAddress address = new CompanyAddress();
            address.setAddressLine1(dto.getAddressLine1());
            address.setCity(dto.getCity());
            address.setStateProvince(dto.getStateProvince());
            address.setPostalCode(dto.getPostalCode());
            address.setAddressCountry(dto.getAddressCountry());
            address.setCounty(dto.getCounty());

            address.setPrimaryContactName(dto.getPrimaryContactName());
            address.setPrimaryContactEmail(dto.getPrimaryContactEmail());
            address.setPosition(dto.getPosition());
            address.setPrimaryContactPhone(dto.getPrimaryContactPhone());
            address.setWebsite(dto.getWebsite());
            address.setPrimaryContactCountry(dto.getPrimaryContactCountry());

            // 3. Set relationship
            address.setCompany(company);

            // 4. Save and return
            return companyAddressRepository.save(address);
    }



    @Transactional
    public OpeningBalanceFileResponse saveOpeningBalanceFile(Long companyId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        // optional: check extension / content type
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Only .csv files are allowed");
        }

        Company company = getCompanyOrThrow(companyId);

        // if you want only ONE file per company, clear old one(s)
        openingBalanceFileRepository.deleteByCompany_Id(companyId);

        CompanyOpeningBalanceFile entity;
        try {
            entity = CompanyOpeningBalanceFile.builder()
                    .company(company)
                    .fileName(originalName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .data(file.getBytes())               // RAW CSV BYTES
                    .uploadedAt(OffsetDateTime.now())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        CompanyOpeningBalanceFile saved = openingBalanceFileRepository.save(entity);
        return OpeningBalanceFileResponse.from(saved);
    }


    // STEP 2 – create/update financial settings (POST)
    @Transactional
    public void upsertFinancialSettings(Long companyId, FinancialSettingsRequest request) {
        Company company = getCompanyOrThrow(companyId);

        CompanyFinancialSettings settings = financialRepo.findByCompany_Id(companyId)
                .orElseGet(() -> CompanyFinancialSettings.builder()
                        .company(company)
                        .build());

        settings.setFiscalYearStartMonth(request.getFiscalYearStartMonth());
        settings.setDefaultArAccountCode(request.getDefaultArAccountCode());
        settings.setRevenueRecognitionMode(request.getRevenueRecognitionMode());
        settings.setDefaultTaxHandling(request.getDefaultTaxHandling());
        settings.setDefaultPaymentTerms(request.getDefaultPaymentTerms());
        settings.setAllowOtherTerms(request.getAllowOtherTerms());
        settings.setEnableCreditLimitChecking(request.getEnableCreditLimitChecking());
        settings.setAgingBucketConfig(request.getAgingBucketConfig());
        settings.setDunningFrequencyDays(request.getDunningFrequencyDays());
        settings.setEnableAutomatedDunningEmails(request.getEnableAutomatedDunningEmails());
        settings.setDefaultCreditLimit(request.getDefaultCreditLimit());

        financialRepo.save(settings);
    }



    //Get Company Details
    public Company getCompanyDetails(Long companyId) {
        return companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found"
                ));
    }



    //Get Company Customers
    // public List<Customer> getCompanyCustomers(Long companyId) {

    //     // Validate company exists
    //     getCompanyDetails(companyId);

    //     List<Customer> customers = companyCustomerRepository.findCustomersByCompanyId(companyId);

    //     return customers;
    // }





    //create Users users
    public Users createCompanyUser(Long companyId, CompanyUserRequest dto) {

        Company company = getCompanyDetails(companyId);

        // Check if the user already exists in the company (both user and company relationship)
        Optional<Users> existingUser = usersRepository.findByEmailAndDeletedFalse(dto.getEmail());

        // If the user exists, check if they are already associated with the company
        if (existingUser.isPresent()) {
            // Check if the user is already associated with the given company
            boolean isUserInCompany = existingUser.get().getUserCompanies().stream()
                .anyMatch(userCompany -> userCompany.getCompany().getId().equals(companyId));

            if (isUserInCompany) {
                // User already exists in the company
                throw new ResponseStatusException(HttpStatus.CONFLICT, "User already present in this company.");
            }
        }


        // Create a new user
        Users user = Users.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .status(UserStatus.PENDING_APPROVAL)  // Set status to INVITED initially
                .build();


        Users savedUser = usersRepository.save(user);


        // Create the UserCompany relationship (i.e., link the user to the company)
        UserCompany userCompany = UserCompany.builder()
                .user(savedUser)
                .company(company)
                .build();


        userCompanyRepository.save(userCompany);


        // Assign roles to the user
        for (Long roleId : dto.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));

            UserRole userRole = UserRole.builder()
                    .user(savedUser)
                    .role(role)
                    .build();

            userRoleRepository.save(userRole);
        }

        // // Generate invite link
        // String inviteLink = "http://3.82.49.182:8080/api/companies/company/users/accept?email="  //backend url
        //         + savedUser.getEmail();


        // // Build the invitation email content
        // String emailHtml = emailTemplateService.buildInviteEmail(
        //         savedUser.getFirstName(),
        //         company.getLegalName(),
        //         inviteLink
        // );

        // // Send the invite email
        // emailService.sendWithAttachment(
        //         savedUser.getEmail(),
        //         "You're invited to join " + company.getLegalName(),
        //         emailHtml,
        //         null
        // );

        return savedUser;
    }



    @Transactional
    public void approveUser(Long userId , Long companyId) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.getStatus() != UserStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User is not pending approval");
        }

        // Change status to INVITED
        user.setStatus(UserStatus.INVITED);
        usersRepository.save(user);


        Company company = getCompanyDetails(companyId);

        // Generate invite link
        String inviteLink = "http://3.82.49.182:8080/api/companies/company/users/accept?email="
                + user.getEmail();

        // Build email
        String emailHtml = emailTemplateService.buildInviteEmail(
                user.getFirstName(),
                company.getLegalName(),
                inviteLink
        );

        // Send email
        emailService.sendWithAttachment(
                user.getEmail(),
                "You're invited to join",
                emailHtml,
                null
        );
    }


    public void validateInvite(String email) {
        Users user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                    

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invite");
        }

        if (user.getStatus() != UserStatus.INVITED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite already used or invalid");
        }
    }
    
    

    //Set password after accept invitation
    @Transactional
    public void acceptInvitation(String email, String password) {

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid invite"));

        if (user.getStatus() != UserStatus.INVITED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invite already used or invalid");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(password));
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordChangedAt(Instant.now());

        usersRepository.save(user);
    }



    //Get User List
    public List<Users> getcompanyUsers(Long companyId) {
        List<Users> users = usersRepository.findByCompany_Id(companyId);

        if(users.isEmpty()){
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "No user found for this company"
            );
        }

        return users;
    }



    public Page<Company> getAllCompanies(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyRepository.findByUserIdAndDeletedFalse(userId, pageable);    
    }



    public CompanyDetailsResponse toDetailsResponse(Company company) {
        Long companyId = company.getId();

        CompanyAddress address = 
                    companyAddressRepository.findByCompany_Id(companyId).orElse(null);

        CompanyFinancialSettings financial =
                financialRepo.findByCompany_Id(companyId).orElse(null);

        CompanyPaymentSettings payment =
                paymentRepo.findByCompany_Id(companyId).orElse(null);

        List<CompanyBankAccount> accounts =
                bankAccountRepo.findByCompanyId(companyId);

        List<Users> users =
            usersRepository.findByCompany_Id(companyId);

        return CompanyDetailsResponse.fromEntities(company, address , financial, payment, accounts , users);
    }



    // STEP 3 – create/update banking + payment (POST)
//    @Transactional
//     public void upsertBankingAndPayment(Long companyId, BankAccountRequest request) {
//     Company company = getCompanyOrThrow(companyId);

    // payment settings (optional in request)
    // PaymentSettingsRequest psReq = request.getPaymentSettings();
    //     if (psReq != null) {
    //         CompanyPaymentSettings paymentSettings = paymentRepo.findByCompany_Id(companyId)
    //                 .orElseGet(() -> CompanyPaymentSettings.builder()
    //                         .company(company)
    //                         .build());

    //         paymentSettings.setAcceptCheck(psReq.getAcceptCheck());
    //         paymentSettings.setAcceptCreditCard(psReq.getAcceptCreditCard());
    //         paymentSettings.setAcceptBankTransfer(psReq.getAcceptBankTransfer());
    //         paymentSettings.setAcceptCash(psReq.getAcceptCash());
    //         paymentSettings.setRemittanceInstructions(psReq.getRemittanceInstructions());

    //         paymentRepo.save(paymentSettings);
    //     }

        // bank accounts (optional)
    //     if (request.getBankAccounts() != null) {
    //         List<CompanyBankAccount> existing = bankAccountRepo.findByCompanyId(companyId);
    //         bankAccountRepo.deleteAll(existing);

    //         for (BankAccountRequest baReq : request.getBankAccounts()) {
    //             CompanyBankAccount acc = CompanyBankAccount.builder()
    //                     .company(company)
    //                     .bankName(baReq.getBankName())
    //                     .accountNumber(baReq.getAccountNumber())
    //                     .ifscSwift(baReq.getIfscSwift())
    //                     .currency(baReq.getCurrency())
    //                     .isDefault(baReq.getIsDefault())
    //                     .build();
    //             bankAccountRepo.save(acc);
    //         }
    //     }
    // }



    // create bank accounts
    @Transactional
    public CompanyBankAccount createBankAccount(
            Long companyId,
            BankAccountRequest request
    ) {

        Company company = getCompanyOrThrow(companyId);

        CompanyBankAccount bankAccount = CompanyBankAccount.builder()
                .company(company)
                .bankName(request.getBankName())
                .accountNumber(request.getAccountNumber())
                .ifscSwift(request.getIfscSwift())
                .currency(request.getCurrency())
                .isDefault(
                    request.getIsDefault() != null
                        ? request.getIsDefault()
                        : false
                )
                .build();

        return bankAccountRepo.save(bankAccount);
    }



    // Update Bank Account
    @Transactional
    public CompanyBankAccount updateBankAccount(
            Long companyId,
            Long bankAccountId,
            BankAccountRequest request
    ) {

        CompanyBankAccount bankAccount =
                bankAccountRepo.findById(bankAccountId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Bank account not found"));

        // Ensure bank account belongs to company
        if (!bankAccount.getCompany().getId().equals(companyId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Bank account does not belong to this company"
            );
        }

        // Update fields only if present
        if (request.getBankName() != null) {
            bankAccount.setBankName(request.getBankName());
        }

        if (request.getAccountNumber() != null) {
            bankAccount.setAccountNumber(request.getAccountNumber());
        }

        if (request.getIfscSwift() != null) {
            bankAccount.setIfscSwift(request.getIfscSwift());
        }

        if (request.getCurrency() != null) {
            bankAccount.setCurrency(request.getCurrency());
        }

        // Handle default logic
        if (request.getIsDefault() != null && request.getIsDefault()) {

            // unset other defaults
            bankAccountRepo.findByCompanyId(companyId)
                .forEach(acc -> {
                    if (!acc.getId().equals(bankAccountId)
                            && Boolean.TRUE.equals(acc.getIsDefault())) {
                        acc.setIsDefault(false);
                        bankAccountRepo.save(acc);
                    }
                });

            bankAccount.setIsDefault(true);
        }

        return bankAccountRepo.save(bankAccount);
    }


    

    public Page<Company> listCompanies(Pageable pageable) {
    return companyRepository.findByDeletedFalse(pageable);
    }


    public Company getCompanyOrThrow(Long id) {
        return companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + id));
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company not found: " + id));

        // already deleted? you can either ignore or throw
        if (company.isDeleted()) {
            return; // or throw new IllegalStateException("Company already deleted");
        }

        company.setDeleted(true);
        companyRepository.save(company);
    }




    // updateCompanyBasic kept for future if you re-enable PUT
    @Transactional
    public Company updateCompanyBasic(Long id, CompanyProfileRequest request) {
        Company company = getCompanyOrThrow(id);
        company.setLegalName(request.getLegalName());
        company.setTradeName(request.getTradeName());
        company.setCompanyCode(request.getCompanyCode());
        company.setCountry(request.getCountry());
        company.setBaseCurrency(request.getBaseCurrency());
        company.setTimeZone(request.getTimeZone());

        return companyRepository.save(company);
    }


    
    @Transactional
    public Company patchCompany(Long companyId, CompanyPatchRequest request) {
        Company company = getCompanyOrThrow(companyId);

        // 1) Patch company base fields
        applyCompanyBasePatch(company, request);
        companyRepository.save(company);

        if (request.getAddress() != null){
            updateAddress(companyId , request.getAddress());
        }

        // 2) Financial
        if (request.getFinancial() != null) {
            patchFinancialSettings(companyId, request.getFinancial());
        }

        // 3) Payment + bank accounts
        if (request.getPayment() != null || request.getBankAccounts() != null) {
            patchBankingAndPaymentFromRoot(
                    companyId,
                    request.getPayment(),
                    request.getBankAccounts()
            );
        }

        // 4) Users
        if (request.getUsers() != null && !request.getUsers().isEmpty()) {
            ManageUsersRequest manageUsersRequest = new ManageUsersRequest();
            manageUsersRequest.setUsers(request.getUsers());
            // upsertCompanyUsers(companyId, manageUsersRequest);
        }

        return getCompanyDetails(companyId);
    }


    private void applyCompanyBasePatch(Company company, CompanyPatchRequest p) {
        if (p.getLegalName() != null)            company.setLegalName(p.getLegalName());
        if (p.getTradeName() != null)            company.setTradeName(p.getTradeName());
        if (p.getCompanyCode() != null)          company.setCompanyCode(p.getCompanyCode());
        if (p.getCountry() != null)              company.setCountry(p.getCountry());
        if (p.getBaseCurrency() != null)         company.setBaseCurrency(p.getBaseCurrency());
        if (p.getTimeZone() != null)             company.setTimeZone(p.getTimeZone());

    }


    public void updateAddress(Long companyId, AddressRequestDto request) {

        Company company = getCompanyOrThrow(companyId);

        CompanyAddress address = companyAddressRepository
                .findByCompany_Id(companyId)
                .orElseGet(() -> CompanyAddress.builder()
                        .company(company)
                        .build());

        if (request.getAddressLine1() != null)
            address.setAddressLine1(request.getAddressLine1());

        if (request.getCity() != null)
            address.setCity(request.getCity());

        if (request.getStateProvince() != null)
            address.setStateProvince(request.getStateProvince());

        if (request.getPostalCode() != null)
            address.setPostalCode(request.getPostalCode());

        if (request.getAddressCountry() != null)
            address.setAddressCountry(request.getAddressCountry());

        if (request.getCounty() != null)
            address.setCounty(request.getCounty());

        if(request.getPosition() != null)
            address.setPosition(request.getPosition());

        if (request.getPrimaryContactName() != null)
            address.setPrimaryContactName(request.getPrimaryContactName());

        if (request.getPrimaryContactEmail() != null)
            address.setPrimaryContactEmail(request.getPrimaryContactEmail());

        if (request.getPrimaryContactPhone() != null)
            address.setPrimaryContactPhone(request.getPrimaryContactPhone());

        if (request.getWebsite() != null)
            address.setWebsite(request.getWebsite());

        if (request.getPrimaryContactCountry() != null)
            address.setPrimaryContactCountry(request.getPrimaryContactCountry());

        company.setCompanyAddress(address);
        companyAddressRepository.save(address);
    }

    @Transactional
    public void patchFinancialSettings(Long companyId, FinancialSettingsRequest request) {
        Company company = getCompanyOrThrow(companyId);

        CompanyFinancialSettings settings = financialRepo.findByCompany_Id(companyId)
                .orElseGet(() -> CompanyFinancialSettings.builder()
                        .company(company)
                        .build());

        if (request.getFiscalYearStartMonth() != null)
            settings.setFiscalYearStartMonth(request.getFiscalYearStartMonth());

        if (request.getDefaultArAccountCode() != null)
            settings.setDefaultArAccountCode(request.getDefaultArAccountCode());

        if (request.getRevenueRecognitionMode() != null)
            settings.setRevenueRecognitionMode(request.getRevenueRecognitionMode());

        if (request.getDefaultTaxHandling() != null)
            settings.setDefaultTaxHandling(request.getDefaultTaxHandling());

        if (request.getDefaultPaymentTerms() != null)
            settings.setDefaultPaymentTerms(request.getDefaultPaymentTerms());

        if (request.getAllowOtherTerms() != null)
            settings.setAllowOtherTerms(request.getAllowOtherTerms());

        if (request.getEnableCreditLimitChecking() != null)
            settings.setEnableCreditLimitChecking(request.getEnableCreditLimitChecking());

        if (request.getAgingBucketConfig() != null)
            settings.setAgingBucketConfig(request.getAgingBucketConfig());

        if (request.getDunningFrequencyDays() != null)
            settings.setDunningFrequencyDays(request.getDunningFrequencyDays());

        if (request.getEnableAutomatedDunningEmails() != null)
            settings.setEnableAutomatedDunningEmails(request.getEnableAutomatedDunningEmails());

        if (request.getDefaultCreditLimit() != null)
            settings.setDefaultCreditLimit(request.getDefaultCreditLimit());

        financialRepo.save(settings);
    }

    @Transactional
    public void patchBankingAndPaymentFromRoot(
            Long companyId,
            PaymentSettingsRequest paymentReq,
            List<BankAccountRequest> bankAccountReqs
    ) {
        Company company = getCompanyOrThrow(companyId);

        // 1) Payment partial patch
        if (paymentReq != null) {
            CompanyPaymentSettings paymentSettings = paymentRepo.findByCompany_Id(companyId)
                    .orElseGet(() -> CompanyPaymentSettings.builder()
                            .company(company)
                            .build());

            if (paymentReq.getAcceptCheck() != null)
                paymentSettings.setAcceptCheck(paymentReq.getAcceptCheck());

            if (paymentReq.getAcceptCreditCard() != null)
                paymentSettings.setAcceptCreditCard(paymentReq.getAcceptCreditCard());

            if (paymentReq.getAcceptBankTransfer() != null)
                paymentSettings.setAcceptBankTransfer(paymentReq.getAcceptBankTransfer());

            if (paymentReq.getAcceptCash() != null)
                paymentSettings.setAcceptCash(paymentReq.getAcceptCash());

            if (paymentReq.getRemittanceInstructions() != null)
                paymentSettings.setRemittanceInstructions(paymentReq.getRemittanceInstructions());

            paymentRepo.save(paymentSettings);
        }

        // 2) Bank accounts – if list is provided, treat it as "replace existing"
        if (bankAccountReqs != null) {
            List<CompanyBankAccount> existing = bankAccountRepo.findByCompanyId(companyId);
            bankAccountRepo.deleteAll(existing);

            for (BankAccountRequest baReq : bankAccountReqs) {
                CompanyBankAccount acc = CompanyBankAccount.builder()
                        .company(company)
                        .bankName(baReq.getBankName())
                        .accountNumber(baReq.getAccountNumber())
                        .ifscSwift(baReq.getIfscSwift())
                        .currency(baReq.getCurrency())
                        .isDefault(baReq.getIsDefault())
                        .build();
                bankAccountRepo.save(acc);
            }
        }
    }


    @Transactional
    public Company updateCompanyContactAndAddress(Long id, CompanyContactAddressRequest req) {
        Company company = getCompanyOrThrow(id);

        // address fields
        // if (req.getAddressLine1() != null)        company.setAddressLine1(req.getAddressLine1());
        // if (req.getCity() != null)                company.setCity(req.getCity());
        // if (req.getStateProvince() != null)       company.setStateProvince(req.getStateProvince());
        // if (req.getPostalCode() != null)          company.setPostalCode(req.getPostalCode());
        // if (req.getAddressCountry() != null)      company.setAddressCountry(req.getAddressCountry());

        // // primary contact fields
        // if (req.getPrimaryContactName() != null)  company.setPrimaryContactName(req.getPrimaryContactName());
        // if (req.getPrimaryContactEmail() != null) company.setPrimaryContactEmail(req.getPrimaryContactEmail());
        // if (req.getPrimaryContactPhone() != null) company.setPrimaryContactPhone(req.getPrimaryContactPhone());
        // if (req.getWebsite() != null)             company.setWebsite(req.getWebsite());
        // if (req.getPrimaryContactCountry() != null)
        //     company.setPrimaryContactCountry(req.getPrimaryContactCountry());

        return companyRepository.save(company);
    }



    //Import User
    @Transactional
    public void importUsers(MultipartFile file, Long companyId) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"));

        try (
                InputStreamReader reader = new InputStreamReader(file.getInputStream());
                CSVParser csvParser = new CSVParser(
                        reader,
                        CSVFormat.DEFAULT
                                .withFirstRecordAsHeader()
                                .withIgnoreHeaderCase()
                                .withTrim()
                )
        ) {

            List<Users> usersToSave = new ArrayList<>();

            for (CSVRecord record : csvParser) {

                String firstName = record.get("firstName");
                String lastName = record.get("lastName");
                String email = record.get("email");

                // Skip if user already exists
                if (usersRepository.existsByEmail(email)) {
                    continue;
                }

                // Create User
                Users user = Users.builder()
                        .firstName(firstName)
                        .lastName(lastName)
                        .email(email)
                        .status(UserStatus.PENDING_APPROVAL)
                        .deleted(false)
                        .forcePasswordChange(false)
                        .mfaEnabled(false)
                        .build();

                // Link User to Company
                UserCompany userCompany = UserCompany.builder()
                        .user(user)
                        .company(company)
                        .build();

                user.setUserCompanies(List.of(userCompany));

                // No roles assigned (keep empty)
                user.setUserRoles(new ArrayList<>());

                usersToSave.add(user);
            }

            usersRepository.saveAll(usersToSave);

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Error processing CSV file",
                    e
            );
        }
    }


    //Assign Role to User
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Role not found"));

        // Optional: Prevent duplicate role assignment
        boolean alreadyAssigned = userRoleRepository
                .existsByUserAndRole(user, role);

        if (alreadyAssigned) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Role already assigned to this user");
        }

        UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .build();

        userRoleRepository.save(userRole);
    }
}


