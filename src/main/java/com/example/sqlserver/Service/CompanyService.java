package com.example.sqlserver.Service;


import com.example.sqlserver.Dto.*;
import com.example.sqlserver.Entity.*;
import com.example.sqlserver.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.OffsetDateTime;


@Service
@RequiredArgsConstructor
public class CompanyService {

     private final CompanyUserRepository companyUserRepository;  
    private final RoleRepository roleRepository;          

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
                // .addressLine1(request.getAddressLine1())
                // .city(request.getCity())
                // .stateProvince(request.getStateProvince())
                // .postalCode(request.getPostalCode())
                // .addressCountry(request.getAddressCountry())
                // .primaryContactName(request.getPrimaryContactName())
                // .primaryContactEmail(request.getPrimaryContactEmail())
                // .primaryContactPhone(request.getPrimaryContactPhone())
                // .website(request.getWebsite())
                // .primaryContactCountry(request.getPrimaryContactCountry())
                .build();

        return companyRepository.save(company);
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



    @Transactional
    public void upsertCompanyUsers(Long companyId, ManageUsersRequest request) {
        Company company = getCompanyOrThrow(companyId);

        if (request == null || request.getUsers() == null || request.getUsers().isEmpty()) {
            return;
        }

        for (CompanyUserRequest dto : request.getUsers()) {

            // 1. Resolve role
            var role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Role not found: " + dto.getRoleId()
                    ));

            CompanyUser entity;

            if (dto.getId() != null) {
                // 2a. UPDATE existing user
                entity = companyUserRepository.findById(dto.getId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "User not found: " + dto.getId()
                        ));

                // Safety: ensure user belongs to this company
                if (!entity.getCompany().getId().equals(companyId)) {
                    throw new IllegalArgumentException(
                            "User " + dto.getId() + " does not belong to company " + companyId
                    );
                }

            } else {
                // 2b. CREATE new user

                // Optional: prevent duplicate email for same company
                companyUserRepository.findByCompany_IdAndEmail(companyId, dto.getEmail())
                        .ifPresent(existing -> {
                            throw new IllegalArgumentException(
                                    "User with email " + dto.getEmail()
                                            + " already exists for company " + companyId
                            );
                        });

                entity = new CompanyUser();
                entity.setCompany(company);
            }

            // 3. Copy fields
            entity.setName(dto.getName());
            entity.setEmail(dto.getEmail());
            entity.setRole(role);
            entity.setStatus(dto.getStatus());

            companyUserRepository.save(entity);
        }
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

    public CompanyDetailsResponse getCompanyDetails(Long companyId) {
        Company company = getCompanyOrThrow(companyId);
        return toDetailsResponse(company);
    }

    public CompanyDetailsResponse toDetailsResponse(Company company) {
        Long companyId = company.getId();

        CompanyFinancialSettings financial =
                financialRepo.findByCompany_Id(companyId).orElse(null);

        CompanyPaymentSettings payment =
                paymentRepo.findByCompany_Id(companyId).orElse(null);

        List<CompanyBankAccount> accounts =
                bankAccountRepo.findByCompanyId(companyId);

        List<CompanyUser> users =
            companyUserRepository.findByCompany_Id(companyId);

        return CompanyDetailsResponse.fromEntities(company, financial, payment, accounts,users);
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
public CompanyDetailsResponse patchCompany(Long companyId, CompanyPatchRequest request) {
    Company company = getCompanyOrThrow(companyId);

    // 1) Patch company base fields
    applyCompanyBasePatch(company, request);
    companyRepository.save(company);

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
        upsertCompanyUsers(companyId, manageUsersRequest);
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

    if (p.getAddressLine1() != null)         company.setAddressLine1(p.getAddressLine1());
    if (p.getCity() != null)                 company.setCity(p.getCity());
    if (p.getStateProvince() != null)        company.setStateProvince(p.getStateProvince());
    if (p.getPostalCode() != null)           company.setPostalCode(p.getPostalCode());
    if (p.getAddressCountry() != null)       company.setAddressCountry(p.getAddressCountry());

    if (p.getPrimaryContactName() != null)   company.setPrimaryContactName(p.getPrimaryContactName());
    if (p.getPrimaryContactEmail() != null)  company.setPrimaryContactEmail(p.getPrimaryContactEmail());
    if (p.getPrimaryContactPhone() != null)  company.setPrimaryContactPhone(p.getPrimaryContactPhone());
    if (p.getWebsite() != null)              company.setWebsite(p.getWebsite());
    if (p.getPrimaryContactCountry() != null) company.setPrimaryContactCountry(p.getPrimaryContactCountry());
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
    if (req.getAddressLine1() != null)        company.setAddressLine1(req.getAddressLine1());
    if (req.getCity() != null)                company.setCity(req.getCity());
    if (req.getStateProvince() != null)       company.setStateProvince(req.getStateProvince());
    if (req.getPostalCode() != null)          company.setPostalCode(req.getPostalCode());
    if (req.getAddressCountry() != null)      company.setAddressCountry(req.getAddressCountry());

    // primary contact fields
    if (req.getPrimaryContactName() != null)  company.setPrimaryContactName(req.getPrimaryContactName());
    if (req.getPrimaryContactEmail() != null) company.setPrimaryContactEmail(req.getPrimaryContactEmail());
    if (req.getPrimaryContactPhone() != null) company.setPrimaryContactPhone(req.getPrimaryContactPhone());
    if (req.getWebsite() != null)             company.setWebsite(req.getWebsite());
    if (req.getPrimaryContactCountry() != null)
        company.setPrimaryContactCountry(req.getPrimaryContactCountry());

    return companyRepository.save(company);
}






}


