package com.example.account.receivable.Customer.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerCsv;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerCsv.RowError;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerDunningCreditSettingsDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerEftDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerFullRequestDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerStatementDTO;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerVatDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CashApplicationUpdateDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerAddressUpdateDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerDunningCreditSettingsUpdateDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerEftUpdateDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerFullUpdateDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerStatementUpdateDTO;
import com.example.account.receivable.Customer.Dto.CustomerUpdateDTO.CustomerVatUpdateDTO;
import com.example.account.receivable.Customer.Entity.CashApplication;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Entity.CustomerAddress;
import com.example.account.receivable.Customer.Entity.CustomerDunningCreditSettings;
import com.example.account.receivable.Customer.Entity.CustomerEFT;
import com.example.account.receivable.Customer.Entity.CustomerStatement;
import com.example.account.receivable.Customer.Entity.CustomerVAT;
import com.example.account.receivable.Customer.Repository.CustomerAddressRepository;
import com.example.account.receivable.Customer.Repository.CustomerCashApplicationRepository;
import com.example.account.receivable.Customer.Repository.CustomerDunningCreditSettingsRepository;
import com.example.account.receivable.Customer.Repository.CustomerEftRepository;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Customer.Repository.CustomerStatementRepository;
import com.example.account.receivable.Customer.Repository.CustomerVatRepository;
import com.example.account.receivable.Exception.DuplicateCustomerException;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;

import jakarta.transaction.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerCashApplicationRepository customerCashApplicationRepository;
    private final CustomerStatementRepository customerStatementRepository;
    private final CustomerEftRepository customerEftRepository;
    private final CustomerVatRepository customerVatRepository;
    private final CustomerDunningCreditSettingsRepository customerDunningCreditSettingsRepository;
    private final InvoiceRepository invoiceRepository;
    private final CompanyRepository companyRepository;

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository,
            CustomerCashApplicationRepository customerCashApplicationRepository,
            CustomerStatementRepository customerStatementRepository,
            CustomerEftRepository customerEftRepository,
            CustomerVatRepository customerVatRepository,
            CustomerDunningCreditSettingsRepository customerDunningCreditSettingsRepository,
            InvoiceRepository invoiceRepository,
            CompanyRepository companyRepository

    ) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.customerCashApplicationRepository = customerCashApplicationRepository;
        this.customerStatementRepository = customerStatementRepository;
        this.customerEftRepository = customerEftRepository;
        this.customerVatRepository = customerVatRepository;
        this.customerDunningCreditSettingsRepository = customerDunningCreditSettingsRepository;
        this.invoiceRepository = invoiceRepository;
        this.companyRepository = companyRepository;
    }


    //Get all customers
    public Page<Customer> getAllCustomers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return customerRepository.findByDeletedFalse(pageable);
    }

    //Get Single Customer
    public Customer getSingleCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        return customer;
    }



    // Method to create a new customer
    public Customer createCustomer(Long companyId , CustomerDTO customerDTO) {
        Company company = companyRepository.findById(companyId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        //EMAIL DUPLICATE CHECK
        customerRepository.findByEmail(customerDTO.getEmail())
            .ifPresent(existing -> {
                throw new DuplicateCustomerException("Customer already exists with this email");
            });

        Long randomNumber = (long) (100000 + new Random().nextInt(900000));
        
        Customer customer = new Customer();
        customer.setCompany(company);
        customer.setCustomerName(customerDTO.getCustomerName());
        customer.setCustomerId(randomNumber);
        customer.setEmail(customerDTO.getEmail());
        customer.setCustomerType(customerDTO.getCustomerType());

        return customerRepository.save(customer);
    }                                                       


    // Method to save customer address
    public CustomerAddress saveCustomerAddress(Long customerId , CustomerAddress customerAddressDTO) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        address.setAddressLine1(customerAddressDTO.getAddressLine1());
        address.setCity(customerAddressDTO.getCity());
        address.setPostalCode(customerAddressDTO.getPostalCode());
        address.setCountry(customerAddressDTO.getCountry());
        address.setStateProvince(customerAddressDTO.getStateProvince());


        return customerAddressRepository.save(address);
    }


    // Method to save CashApplication data and link it to the customer
    public CashApplication saveCashApplication(Long customerId , CashApplication cashApplicationDTO) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CashApplication cashApplication = new CashApplication();
        cashApplication.setApplyPayments(cashApplicationDTO.isApplyPayments());
        cashApplication.setAutoApplyPayments(cashApplicationDTO.isAutoApplyPayments());
        cashApplication.setToleranceAmount(cashApplicationDTO.getToleranceAmount());
        cashApplication.setTolerancePercentage(cashApplicationDTO.getTolerancePercentage());
        cashApplication.setShipCreditCheck(cashApplicationDTO.isShipCreditCheck());
        cashApplication.setCustomer(customer); // Link CashApplication to Customer


        return customerCashApplicationRepository.save(cashApplication);
    }



    // Method to save CustomerStatement data and link it to the customer
    public CustomerStatement saveCustomerStatement(Long customerId , CustomerStatementDTO customerStatementDTO) {
        // Retrieve the customer entity using the customerId
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CustomerStatement customerStatement = new CustomerStatement();
        customerStatement.setSendStatements(customerStatementDTO.isSendStatements());
        customerStatement.setAutoApplyPayments(customerStatementDTO.isAutoApplyPayments());
        customerStatement.setTolerancePercentage(customerStatementDTO.getTolerancePercentage());
        customerStatement.setMinimumAmount(customerStatementDTO.getMinimumAmount());
        customerStatement.setCustomer(customer); // Link CustomerStatement to Customer

        // Save CustomerStatement in the database
        return customerStatementRepository.save(customerStatement);
    }

    // Method to save EFT data
    public CustomerEFT saveCustomerEft(Long customerId , CustomerEftDTO customerEftDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CustomerEFT eft = new CustomerEFT();
        eft.setCustomer(customer);
        eft.setBankName(customerEftDTO.getBankName());
        eft.setIbanAccountNumber(customerEftDTO.getIbanAccountNumber());
        eft.setBankIdentifierCode(customerEftDTO.getBankIdentifierCode());
        eft.setEnableAchPayments(customerEftDTO.isEnableAchPayments());

        return customerEftRepository.save(eft);
    }

    // Method to save VAT data
    public CustomerVAT saveCustomerVat(Long customerId , CustomerVatDTO customerVatDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CustomerVAT vat = new CustomerVAT();
        vat.setCustomer(customer);
        vat.setTaxIdentificationNumber(customerVatDTO.getTaxIdentificationNumber());
        vat.setTaxAgencyName(customerVatDTO.getTaxAgencyName());
        vat.setEnableVatCodes(customerVatDTO.isEnableVatCodes());

        return customerVatRepository.save(vat);
    }



    // Method to save Dunning & Credit Settings
    public CustomerDunningCreditSettings saveCustomerDunningCreditSettings(Long customerId , CustomerDunningCreditSettingsDTO customerDunningCreditSettingsDTO) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        CustomerDunningCreditSettings dunningCreditSettings = new CustomerDunningCreditSettings();
        dunningCreditSettings.setCustomer(customer);
        dunningCreditSettings.setPlaceOnCreditHold(customerDunningCreditSettingsDTO.isPlaceOnCreditHold());
        dunningCreditSettings.setCreditLimit(customerDunningCreditSettingsDTO.getCreditLimit());
        dunningCreditSettings.setDunningLevel(customerDunningCreditSettingsDTO.getDunningLevel());
        dunningCreditSettings.setPastDue(customerDunningCreditSettingsDTO.getPastDue());
        dunningCreditSettings.setLevel1(customerDunningCreditSettingsDTO.getLevel1());
        dunningCreditSettings.setLevel2(customerDunningCreditSettingsDTO.getLevel2());
        dunningCreditSettings.setLevel3(customerDunningCreditSettingsDTO.getLevel3());
        dunningCreditSettings.setLevel4(customerDunningCreditSettingsDTO.getLevel4());

        return customerDunningCreditSettingsRepository.save(dunningCreditSettings);
    }



    // Method to soft delete a customer (set is_deleted = true)
    public Customer softDeleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        customer.setDeleted(true);
        
        List<Invoice> invoices = invoiceRepository.findByCustomerId(customerId);
        
        if (!invoices.isEmpty()) {
            invoices.forEach(inv -> inv.setDeleted(true));
            invoiceRepository.saveAll(invoices);
        };
        
        return customerRepository.save(customer);
    }



    @Transactional
    public Customer createCompleteCustomer(CustomerFullRequestDTO request) {

    // EMAIL DUPLICATE CHECK
    customerRepository.findByEmail(request.getEmail())
            .ifPresent(existing -> {
                throw new DuplicateCustomerException("Customer already exists with this email");
            });
        
        Long randomNumber = (long) (100000 + new Random().nextInt(900000));

        Customer customer = new Customer();
        customer.setCustomerName(request.getCustomerName());
        customer.setCustomerId(randomNumber);
        customer.setEmail(request.getEmail());
        customer.setCustomerType(request.getCustomerType());
        customer = customerRepository.save(customer);

        //Save Address(es)
        if (request.getAddresses() != null) {
            CustomerAddress address = new CustomerAddress();
            address.setCustomer(customer);
            address.setAddressLine1(address.getAddressLine1());
            address.setCity(address.getCity());
            address.setPostalCode(address.getPostalCode());
            address.setCountry(address.getCountry());
            address.setStateProvince(address.getStateProvince());
            customerAddressRepository.save(address);
        }

        // Cash Application
        if (request.getCashApplication() != null) {
            CashApplication cash = new CashApplication();
            cash.setCustomer(customer);
            cash.setApplyPayments(request.getCashApplication().isApplyPayments());
            cash.setAutoApplyPayments(request.getCashApplication().isAutoApplyPayments());
            cash.setToleranceAmount(request.getCashApplication().getToleranceAmount());
            cash.setTolerancePercentage(request.getCashApplication().getTolerancePercentage());
            cash.setShipCreditCheck(request.getCashApplication().isShipCreditCheck());
            customerCashApplicationRepository.save(cash);
        }

        // Statement
        if (request.getStatement() != null) {
            CustomerStatement stmt = new CustomerStatement();
            stmt.setCustomer(customer);
            stmt.setSendStatements(request.getStatement().isSendStatements());
            stmt.setAutoApplyPayments(request.getStatement().isAutoApplyPayments());
            stmt.setTolerancePercentage(request.getStatement().getTolerancePercentage());
            stmt.setMinimumAmount(request.getStatement().getMinimumAmount());
            customerStatementRepository.save(stmt);
        }

        // EFT
        if (request.getEft() != null) {
            CustomerEFT eft = new CustomerEFT();
            eft.setCustomer(customer);
            eft.setBankName(request.getEft().getBankName());
            eft.setIbanAccountNumber(request.getEft().getIbanAccountNumber());
            eft.setBankIdentifierCode(request.getEft().getBankIdentifierCode());
            eft.setEnableAchPayments(request.getEft().isEnableAchPayments());
            customerEftRepository.save(eft);
        }

        // VAT
        if (request.getVat() != null) {
            CustomerVAT vat = new CustomerVAT();
            vat.setCustomer(customer);
            vat.setTaxIdentificationNumber(request.getVat().getTaxIdentificationNumber());
            vat.setTaxAgencyName(request.getVat().getTaxAgencyName());
            vat.setEnableVatCodes(request.getVat().isEnableVatCodes());
            customerVatRepository.save(vat);
        }

        // Dunning & Credit
        if (request.getDunningCredit() != null) {
            CustomerDunningCreditSettings dc = new CustomerDunningCreditSettings();
            dc.setCustomer(customer);
            dc.setPlaceOnCreditHold(request.getDunningCredit().isPlaceOnCreditHold());
            dc.setCreditLimit(request.getDunningCredit().getCreditLimit());
            dc.setDunningLevel(request.getDunningCredit().getDunningLevel());
            dc.setPastDue(request.getDunningCredit().getPastDue());
            dc.setLevel1(request.getDunningCredit().getLevel1());
            dc.setLevel2(request.getDunningCredit().getLevel2());
            dc.setLevel3(request.getDunningCredit().getLevel3());
            dc.setLevel4(request.getDunningCredit().getLevel4());
            customerDunningCreditSettingsRepository.save(dc);
        }

        return customer;
    }



    //Helper Method for the Update
    private <T> void updateIfNotNull(T value, Consumer<T> setter) {
        if (value != null) setter.accept(value);
    }

    //Update Customer Method
    @Transactional
    public Customer updateCustomer(Long customerId, CustomerFullUpdateDTO dto) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
                
        // --- MAIN CUSTOMER FIELDS ---
        updateIfNotNull(dto.getCustomerName(),  customer::setCustomerName);
        updateIfNotNull(dto.getEmail(),        customer::setEmail);
        updateIfNotNull(dto.getCustomerType(), customer::setCustomerType);


        // --- ADDRESS ---
        if (dto.getAddresses() != null) {

            CustomerAddressUpdateDTO addrDto = dto.getAddresses();

            CustomerAddress address = customerAddressRepository.findByCustomer_Id(customerId)
                    .orElseGet(() -> {
                        CustomerAddress a = new CustomerAddress();
                        a.setCustomer(customer);
                        return a;
                    });


            // if (dto.getCustomerName() != null) 
            //     customer.setCustomerName(dto.getCustomerName());
            // if (dto.getCustomerId() != null) 
            //     customer.setCustomerId(dto.getCustomerId());
            // if (dto.getEmail() != null) 
            //     customer.setEmail(dto.getEmail());
            // if (dto.getCustomerType() != null) 
            //     customer.setCustomerType(dto.getCustomerType());

            updateIfNotNull(addrDto.getAddressLine1(), address::setAddressLine1);
            updateIfNotNull(addrDto.getCity(),         address::setCity);
            updateIfNotNull(addrDto.getPostalCode(),   address::setPostalCode);
            updateIfNotNull(addrDto.getCountry(),      address::setCountry);
            updateIfNotNull(addrDto.getStateProvince(), address::setStateProvince);

            customerAddressRepository.save(address);
        }


        // --- CASH APPLICATION ---
        if (dto.getCashApplication() != null) {
            CashApplicationUpdateDTO cashDto = dto.getCashApplication();

            CashApplication cash = customerCashApplicationRepository.findByCustomer_Id(customerId)
                    .orElseGet(() -> {
                        CashApplication c = new CashApplication();
                        c.setCustomer(customer);
                        return c;
                    });

            updateIfNotNull(cashDto.getApplyPayments(),       cash::setApplyPayments);
            updateIfNotNull(cashDto.getAutoApplyPayments(),   cash::setAutoApplyPayments);
            updateIfNotNull(cashDto.getToleranceAmount(),     cash::setToleranceAmount);
            updateIfNotNull(cashDto.getTolerancePercentage(), cash::setTolerancePercentage);
            updateIfNotNull(cashDto.getShipCreditCheck(),     cash::setShipCreditCheck);

            customerCashApplicationRepository.save(cash);
        }


        // --- STATEMENT ---
        if (dto.getStatement() != null) {
            CustomerStatementUpdateDTO stmtDto = dto.getStatement();

            CustomerStatement stmt = customerStatementRepository.findByCustomer_Id(customerId)
                    .orElseGet(() -> {
                        CustomerStatement s = new CustomerStatement();
                        s.setCustomer(customer);
                        return s;
                    });

            updateIfNotNull(stmtDto.getSendStatements(),       stmt::setSendStatements);
            updateIfNotNull(stmtDto.getAutoApplyPayments(),    stmt::setAutoApplyPayments);
            updateIfNotNull(stmtDto.getTolerancePercentage(),  stmt::setTolerancePercentage);
            updateIfNotNull(stmtDto.getMinimumAmount(),        stmt::setMinimumAmount);

            customerStatementRepository.save(stmt);
        }


        // --- EFT ---
        if (dto.getEft() != null) {
            CustomerEftUpdateDTO eftDto = dto.getEft();

            CustomerEFT eft = customerEftRepository.findByCustomer_Id(customerId)
                    .orElseGet(() -> {
                        CustomerEFT e = new CustomerEFT();
                        e.setCustomer(customer);
                        return e;
                    });

            updateIfNotNull(eftDto.getBankName(),          eft::setBankName);
            updateIfNotNull(eftDto.getIbanAccountNumber(), eft::setIbanAccountNumber);
            updateIfNotNull(eftDto.getBankIdentifierCode(), eft::setBankIdentifierCode);
            updateIfNotNull(eftDto.getEnableAchPayments(), eft::setEnableAchPayments);

            customerEftRepository.save(eft);
        }


        // --- VAT ---
        if (dto.getVat() != null) {
            CustomerVatUpdateDTO vatDto = dto.getVat();

            CustomerVAT vat = customerVatRepository.findByCustomer_Id(customerId)
                    .orElseGet(() -> {
                        CustomerVAT v = new CustomerVAT();
                        v.setCustomer(customer);
                        return v;
                    });

            updateIfNotNull(vatDto.getTaxIdentificationNumber(), vat::setTaxIdentificationNumber);
            updateIfNotNull(vatDto.getTaxAgencyName(),           vat::setTaxAgencyName);
            updateIfNotNull(vatDto.getEnableVatCodes(),          vat::setEnableVatCodes);

            customerVatRepository.save(vat);
        }


        // --- DUNNING / CREDIT SETTINGS ---
        if (dto.getDunningCredit() != null) {
            CustomerDunningCreditSettingsUpdateDTO dcDto = dto.getDunningCredit();

            CustomerDunningCreditSettings dc = customerDunningCreditSettingsRepository.findByCustomer_Id(customerId)
                    .orElseGet(() -> {
                        CustomerDunningCreditSettings d = new CustomerDunningCreditSettings();
                        d.setCustomer(customer);
                        return d;
                    });

            updateIfNotNull(dcDto.getPlaceOnCreditHold(), dc::setPlaceOnCreditHold);
            updateIfNotNull(dcDto.getCreditLimit(),       dc::setCreditLimit);
            updateIfNotNull(dcDto.getDunningLevel(),      dc::setDunningLevel);
            updateIfNotNull(dcDto.getPastDue(),           dc::setPastDue);
            updateIfNotNull(dcDto.getLevel1(),            dc::setLevel1);
            updateIfNotNull(dcDto.getLevel2(),            dc::setLevel2);
            updateIfNotNull(dcDto.getLevel3(),            dc::setLevel3);
            updateIfNotNull(dcDto.getLevel4(),            dc::setLevel4);

            customerDunningCreditSettingsRepository.save(dc);
        }

        return customerRepository.save(customer);
    }

    @Transactional
    public CustomerCsv importCustomersFromCsv(Long companyId, MultipartFile file) {
    if (file == null || file.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
    }

    Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

    int total = 0;
    int success = 0;
    List<RowError> errors = new ArrayList<>();

    try (Reader reader = new BufferedReader(
            new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

        CSVParser parser = CSVFormat.DEFAULT
                .withFirstRecordAsHeader()
                .withIgnoreHeaderCase()
                .withTrim()
                .withIgnoreEmptyLines()
                .parse(reader);

        HeaderMapper headerMapper = new HeaderMapper(parser.getHeaderMap().keySet());

        for (CSVRecord record : parser) {
            total++;
            long rowNumber = record.getRecordNumber(); // 1 = first data row

            try {
                importSingleCustomerRecord(company, record, headerMapper);
                success++;
            } catch (DuplicateCustomerException ex) {
                errors.add(RowError.builder()
                        .rowNumber(rowNumber)
                        .message("Duplicate customer: " + ex.getMessage())
                        .build());
            } catch (IllegalArgumentException ex) {
                errors.add(RowError.builder()
                        .rowNumber(rowNumber)
                        .message("Validation error: " + ex.getMessage())
                        .build());
            } catch (Exception ex) {
                errors.add(RowError.builder()
                        .rowNumber(rowNumber)
                        .message("Unexpected error: " + ex.getMessage())
                        .build());
            }
        }

    } catch (IOException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read CSV file", e);
    }

    int failure = total - success;

    return CustomerCsv.builder()
            .totalRows(total)
            .successCount(success)
            .failureCount(failure)
            .errors(errors)
            .build();
}
private void importSingleCustomerRecord(
        Company company,
        CSVRecord record,
        HeaderMapper headers
) {
    // ---- BASIC CUSTOMER DATA ----
    String email = headers.get(record, "email");
    String name = headers.get(record, "customerName");
    String customerType = headers.get(record, "customerType");

    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email is required");
    }
    if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Customer name is required");
    }

    // Duplicate check by email
    customerRepository.findByEmail(email)
            .ifPresent(existing -> {
                throw new DuplicateCustomerException(
                        "Customer already exists with email: " + email
                );
            });

    Long randomNumber = (long) (100000 + new Random().nextInt(900000));

    Customer customer = new Customer();
    customer.setCompany(company);
    customer.setCustomerName(name);
    customer.setCustomerId(randomNumber);
    customer.setEmail(email);
    customer.setCustomerType(customerType);
    customer.setDeleted(false);

    customer = customerRepository.save(customer);

    // ---- ADDRESS (OPTIONAL) ----
    String addressLine1 = headers.get(record, "addressLine1");
    String city = headers.get(record, "city");
    String postalCode = headers.get(record, "postalCode");
    String country = headers.get(record, "country");
    String stateProvince = headers.get(record, "stateProvince");

    if (anyNonBlank(addressLine1, city, postalCode, country, stateProvince)) {
        CustomerAddress addr = new CustomerAddress();
        addr.setCustomer(customer);
        addr.setAddressLine1(addressLine1);
        addr.setCity(city);
        addr.setPostalCode(postalCode);
        addr.setCountry(country);
        addr.setStateProvince(stateProvince);
        customerAddressRepository.save(addr);
    }

    // ---- CASH APPLICATION (OPTIONAL) ----
    Boolean applyPayments       = headers.getBoolean(record, "applyPayments");
    Boolean autoApplyPayments   = headers.getBoolean(record, "autoApplyPayments");
    Double toleranceAmount      = headers.getDouble(record, "toleranceAmount");
    Double tolerancePercentage  = headers.getDouble(record, "tolerancePercentage");
    Boolean shipCreditCheck     = headers.getBoolean(record, "shipCreditCheck");

    if (applyPayments != null || autoApplyPayments != null
            || toleranceAmount != null || tolerancePercentage != null
            || shipCreditCheck != null) {

        CashApplication cash = new CashApplication();
        cash.setCustomer(customer);
        cash.setApplyPayments(applyPayments != null && applyPayments);
        cash.setAutoApplyPayments(autoApplyPayments != null && autoApplyPayments);
        cash.setToleranceAmount(toleranceAmount);
        cash.setTolerancePercentage(tolerancePercentage);
        cash.setShipCreditCheck(shipCreditCheck != null && shipCreditCheck);
        customerCashApplicationRepository.save(cash);
    }

    // ---- STATEMENT (OPTIONAL) ----
    Boolean sendStatements      = headers.getBoolean(record, "sendStatements");
    Boolean stmtAutoApply       = headers.getBoolean(record, "statementAutoApplyPayments");
    Double stmtTolerancePercent = headers.getDouble(record, "statementTolerancePercentage");
    Double minimumAmount        = headers.getDouble(record, "statementMinimumAmount");

    if (sendStatements != null || stmtAutoApply != null
            || stmtTolerancePercent != null || minimumAmount != null) {

        CustomerStatement stmt = new CustomerStatement();
        stmt.setCustomer(customer);
        stmt.setSendStatements(sendStatements != null && sendStatements);
        stmt.setAutoApplyPayments(stmtAutoApply != null && stmtAutoApply);
        stmt.setTolerancePercentage(stmtTolerancePercent);
        stmt.setMinimumAmount(minimumAmount);
        customerStatementRepository.save(stmt);
    }

    // ---- EFT (OPTIONAL) ----
    String bankName            = headers.get(record, "bankName");
    String ibanAccountNumber   = headers.get(record, "ibanAccountNumber");
    String bankIdentifierCode  = headers.get(record, "bankIdentifierCode");
    Boolean enableAchPayments  = headers.getBoolean(record, "enableAchPayments");

    if (anyNonBlank(bankName, ibanAccountNumber, bankIdentifierCode)
            || enableAchPayments != null) {

        CustomerEFT eft = new CustomerEFT();
        eft.setCustomer(customer);
        eft.setBankName(bankName);
        eft.setIbanAccountNumber(ibanAccountNumber);
        eft.setBankIdentifierCode(bankIdentifierCode);
        eft.setEnableAchPayments(enableAchPayments != null && enableAchPayments);
        customerEftRepository.save(eft);
    }

    // ---- VAT (OPTIONAL) ----
    String taxId        = headers.get(record, "taxIdentificationNumber");
    String taxAgency    = headers.get(record, "taxAgencyName");
    Boolean enableVat   = headers.getBoolean(record, "enableVatCodes");

    if (anyNonBlank(taxId, taxAgency) || enableVat != null) {
        CustomerVAT vat = new CustomerVAT();
        vat.setCustomer(customer);
        vat.setTaxIdentificationNumber(taxId);
        vat.setTaxAgencyName(taxAgency);
        vat.setEnableVatCodes(enableVat != null && enableVat);
        customerVatRepository.save(vat);
    }

    // ---- DUNNING / CREDIT (OPTIONAL) ----
    Boolean placeOnCreditHold  = headers.getBoolean(record, "placeOnCreditHold");
    Double creditLimit         = headers.getDouble(record, "creditLimit");
    String dunningLevel        = headers.get(record, "dunningLevel");
    String pastDue             = headers.get(record, "pastDue");
    String level1              = headers.get(record, "level1");
    String level2              = headers.get(record, "level2");
    String level3              = headers.get(record, "level3");
    String level4              = headers.get(record, "level4");

    if (placeOnCreditHold != null || creditLimit != null ||
            anyNonBlank(dunningLevel, pastDue, level1, level2, level3, level4)) {

        CustomerDunningCreditSettings dc = new CustomerDunningCreditSettings();
        dc.setCustomer(customer);
        dc.setPlaceOnCreditHold(placeOnCreditHold != null && placeOnCreditHold);
        dc.setCreditLimit(creditLimit);
        dc.setDunningLevel(dunningLevel);
        dc.setPastDue(pastDue);
        dc.setLevel1(level1);
        dc.setLevel2(level2);
        dc.setLevel3(level3);
        dc.setLevel4(level4);
        customerDunningCreditSettingsRepository.save(dc);
    }
}

private static class HeaderMapper {

    private final Map<String, String> canonicalToCsvHeader;

    // Known aliases → canonical field names
    private static final Map<String, String> ALIASES;

    static {
        Map<String, String> m = new HashMap<>();

        // Customer
        m.put("customername", "customerName");
        m.put("name", "customerName");
        m.put("customer_name", "customerName");

        m.put("email", "email");
        m.put("emailaddress", "email");

        m.put("customertype", "customerType");
        m.put("customer_type", "customerType");

        // Address
        m.put("address", "addressLine1");
        m.put("addressline1", "addressLine1");
        m.put("street", "addressLine1");

        m.put("city", "city");
        m.put("postalcode", "postalCode");
        m.put("zipcode", "postalCode");
        m.put("zip", "postalCode");

        m.put("country", "country");
        m.put("state", "stateProvince");
        m.put("stateprovince", "stateProvince");

        // VAT / Tax
        m.put("taxnumber", "taxIdentificationNumber");
        m.put("taxno", "taxIdentificationNumber");
        m.put("taxidentificationnumber", "taxIdentificationNumber");

        m.put("taxagency", "taxAgencyName");
        m.put("taxagencyname", "taxAgencyName");

        // EFT
        m.put("bankname", "bankName");
        m.put("iban", "ibanAccountNumber");
        m.put("ibanaccountnumber", "ibanAccountNumber");
        m.put("bankidentifiercode", "bankIdentifierCode");
        m.put("bic", "bankIdentifierCode");

        // Booleans examples
        m.put("enablevatcodes", "enableVatCodes");
        m.put("enableachpayments", "enableAchPayments");
        m.put("applypayments", "applyPayments");
        m.put("autoapplypayments", "autoApplyPayments");
        m.put("shipcreditcheck", "shipCreditCheck");
        m.put("sendstatements", "sendStatements");

        // Statement-specific
        m.put("statementautoapplypayments", "statementAutoApplyPayments");
        m.put("statementtolerancepercentage", "statementTolerancePercentage");
        m.put("statementminimumamount", "statementMinimumAmount");

        // Dunning
        m.put("placeoncredithold", "placeOnCreditHold");
        m.put("creditlimit", "creditLimit");
        m.put("dunninglevel", "dunningLevel");
        m.put("pastdue", "pastDue");
        m.put("level1", "level1");
        m.put("level2", "level2");
        m.put("level3", "level3");
        m.put("level4", "level4");

        ALIASES = Collections.unmodifiableMap(m);
    }

    HeaderMapper(Set<String> csvHeaders) {
        this.canonicalToCsvHeader = csvHeaders.stream()
                .collect(Collectors.toMap(
                        this::toCanonicalFromCsvHeader,
                        h -> h,
                        // if multiple match same canonical, keep the first
                        (existing, replacement) -> existing
                ));
    }

    private String toCanonicalFromCsvHeader(String header) {
        String normalized = normalize(header);
        return ALIASES.getOrDefault(normalized, normalized);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim()
                .toLowerCase()
                .replace(" ", "")
                .replace("_", "")
                .replace("-", "");
    }

    // Get raw string
    public String get(CSVRecord record, String canonicalField) {
        String header = canonicalToCsvHeader.get(canonicalField);
        if (header == null) return null;

        String value = record.get(header);
        if (value == null) return null;

        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    public Boolean getBoolean(CSVRecord record, String canonicalField) {
        String value = get(record, canonicalField);
        if (value == null) return null;

        String v = value.trim().toLowerCase();
        if (v.isEmpty()) return null;
        if (v.equals("true") || v.equals("yes") || v.equals("y") || v.equals("1")) return true;
        if (v.equals("false") || v.equals("no") || v.equals("n") || v.equals("0")) return false;

        throw new IllegalArgumentException("Invalid boolean value '" + value + "' for field " + canonicalField);
    }

    public Double getDouble(CSVRecord record, String canonicalField) {
        String value = get(record, canonicalField);
        if (value == null) return null;
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number value '" + value + "' for field " + canonicalField);
        }
    }
}

private boolean anyNonBlank(String... values) {
    if (values == null) return false;
    for (String v : values) {
        if (v != null && !v.trim().isEmpty()) {
            return true;
        }
    }
    return false;
}



}
