package com.example.account.receivable.Company.Service;


import com.example.account.receivable.CommomRepository.CompanyCustomerRepository;
import com.example.account.receivable.Company.Dto.*;
import com.example.account.receivable.Company.Entity.*;
import com.example.account.receivable.Company.Repository.*;
import com.example.account.receivable.Customer.Entity.Customer;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyUserRepository companyUserRepository;  
    private final RoleRepository roleRepository;
    private final CompanyAddressRepository companyAddressRepository;   
    private final CompanyCustomerRepository companyCustomerRepository;
       

    private final CompanyRepository companyRepository;
    private final CompanyFinancialSettingsRepository financialRepo;
    private final CompanyPaymentSettingsRepository paymentRepo;
    private final CompanyBankAccountRepository bankAccountRepo;
    private final CompanyOpeningBalanceFileRepository openingBalanceFileRepository; 


    // STEP 1 – create company
    @Transactional
    public Company createCompanyStep1(CompanyProfileRequest request) {
        Company company = Company.builder()
                .legalName(request.getLegalName())
                .tradeName(request.getTradeName())
                .companyCode(request.getCompanyCode())
                .country(request.getCountry())
                .baseCurrency(request.getBaseCurrency())
                .timeZone(request.getTimeZone())
                .build();

        return companyRepository.save(company);
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

            address.setPrimaryContactName(dto.getPrimaryContactName());
            address.setPrimaryContactEmail(dto.getPrimaryContactEmail());
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
    public List<Customer> getCompanyCustomers(Long companyId) {

        // Validate company exists
        getCompanyDetails(companyId);

        List<Customer> customers = companyCustomerRepository.findCustomersByCompanyId(companyId);

        return customers;
    }





    //create company users
    public CompanyUser createCompanyUser(Long companyId , CompanyUserRequest dto){

        //check company
        Company company = getCompanyDetails(companyId);

        CompanyUser existingUser = companyUserRepository.findByEmail(dto.getEmail());
        if (existingUser != null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,   // 409
                "User already exists with this email"
            );
        }
        

        //Check Role 
        Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Role not found"
                    ));

        CompanyUser user =  CompanyUser.builder()
                            .company(company)
                            .role(role)
                            .name(dto.getName())
                            .email(dto.getEmail())
                            .status(dto.getStatus())
                            .build();

        return companyUserRepository.save(user);

    }



    //Get User List
    public List<CompanyUser> getcompanyUsers(Long companyId) {
        List<CompanyUser> users = companyUserRepository.findByCompany_Id(companyId);

        if(users.isEmpty()){
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "No user found for this company"
            );
        }

        return users;
    }



    public Page<Company> getAllCompanies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return companyRepository.findByDeletedFalse(pageable);    
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

        List<CompanyUser> users =
            companyUserRepository.findByCompany_Id(companyId);

        return CompanyDetailsResponse.fromEntities(company, address , financial, payment, accounts,users);
    }



    // STEP 3 – create/update banking + payment (POST)
   @Transactional
    public void upsertBankingAndPayment(Long companyId, BankingStepRequest request) {
    Company company = getCompanyOrThrow(companyId);

    // payment settings (optional in request)
    PaymentSettingsRequest psReq = request.getPaymentSettings();
        if (psReq != null) {
            CompanyPaymentSettings paymentSettings = paymentRepo.findByCompany_Id(companyId)
                    .orElseGet(() -> CompanyPaymentSettings.builder()
                            .company(company)
                            .build());

            paymentSettings.setAcceptCheck(psReq.getAcceptCheck());
            paymentSettings.setAcceptCreditCard(psReq.getAcceptCreditCard());
            paymentSettings.setAcceptBankTransfer(psReq.getAcceptBankTransfer());
            paymentSettings.setAcceptCash(psReq.getAcceptCash());
            paymentSettings.setRemittanceInstructions(psReq.getRemittanceInstructions());

            paymentRepo.save(paymentSettings);
        }

        // bank accounts (optional)
        if (request.getBankAccounts() != null) {
            List<CompanyBankAccount> existing = bankAccountRepo.findByCompanyId(companyId);
            bankAccountRepo.deleteAll(existing);

            for (BankAccountRequest baReq : request.getBankAccounts()) {
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
        // company.setAddressLine1(request.getAddressLine1());
        // company.setCity(request.getCity());
        // company.setStateProvince(request.getStateProvince());
        // company.setPostalCode(request.getPostalCode());
        // company.setAddressCountry(request.getAddressCountry());
        // company.setPrimaryContactName(request.getPrimaryContactName());
        // company.setPrimaryContactEmail(request.getPrimaryContactEmail());
        // company.setPrimaryContactPhone(request.getPrimaryContactPhone());
        // company.setWebsite(request.getWebsite());
        // company.setPrimaryContactCountry(request.getPrimaryContactCountry());
        return companyRepository.save(company);
    }


    
    @Transactional
    public Company patchCompany(Long companyId, CompanyPatchRequest request) {
        Company company = getCompanyOrThrow(companyId);

        // 1) Patch company base fields
        applyCompanyBasePatch(company, request);
        companyRepository.save(company);

        if (request.getAddress() != null){
            UpdateAddress(companyId , request.getAddress());
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

        // if (p.getAddressLine1() != null)         company.setAddressLine1(p.getAddressLine1());
        // if (p.getCity() != null)                 company.setCity(p.getCity());
        // if (p.getStateProvince() != null)        company.setStateProvince(p.getStateProvince());
        // if (p.getPostalCode() != null)           company.setPostalCode(p.getPostalCode());
        // if (p.getAddressCountry() != null)       company.setAddressCountry(p.getAddressCountry());

        // if (p.getPrimaryContactName() != null)   company.setPrimaryContactName(p.getPrimaryContactName());
        // if (p.getPrimaryContactEmail() != null)  company.setPrimaryContactEmail(p.getPrimaryContactEmail());
        // if (p.getPrimaryContactPhone() != null)  company.setPrimaryContactPhone(p.getPrimaryContactPhone());
        // if (p.getWebsite() != null)              company.setWebsite(p.getWebsite());
        // if (p.getPrimaryContactCountry() != null) company.setPrimaryContactCountry(p.getPrimaryContactCountry());
    }


    public void UpdateAddress(Long companyId , AddressRequestDto request){
        Company company = getCompanyOrThrow(companyId);

        CompanyAddress address = companyAddressRepository.findByCompany_Id(companyId)
                        .orElseGet(() -> CompanyAddress.builder()
                        .company(company)
                        .build());


        // 3. Map DTO → Entity
        address.setAddressLine1(request.getAddressLine1());
        address.setCity(request.getCity());
        address.setStateProvince(request.getStateProvince());
        address.setPostalCode(request.getPostalCode());
        address.setAddressCountry(request.getAddressCountry());
        address.setPrimaryContactName(request.getPrimaryContactName());
        address.setPrimaryContactEmail(request.getPrimaryContactEmail());
        address.setPrimaryContactPhone(request.getPrimaryContactPhone());
        address.setWebsite(request.getWebsite());
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






}


