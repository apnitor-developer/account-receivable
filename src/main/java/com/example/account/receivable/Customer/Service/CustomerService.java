package com.example.account.receivable.Customer.Service;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

    public CustomerService(
            CustomerRepository customerRepository,
            CustomerAddressRepository customerAddressRepository,
            CustomerCashApplicationRepository customerCashApplicationRepository,
            CustomerStatementRepository customerStatementRepository,
            CustomerEftRepository customerEftRepository,
            CustomerVatRepository customerVatRepository,
            CustomerDunningCreditSettingsRepository customerDunningCreditSettingsRepository,
            InvoiceRepository invoiceRepository

    ) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.customerCashApplicationRepository = customerCashApplicationRepository;
        this.customerStatementRepository = customerStatementRepository;
        this.customerEftRepository = customerEftRepository;
        this.customerVatRepository = customerVatRepository;
        this.customerDunningCreditSettingsRepository = customerDunningCreditSettingsRepository;
        this.invoiceRepository = invoiceRepository;
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
    public Customer createCustomer(CustomerDTO customerDTO) {

        //EMAIL DUPLICATE CHECK
        customerRepository.findByEmail(customerDTO.getEmail())
            .ifPresent(existing -> {
                throw new DuplicateCustomerException("Customer already exists with this email");
            });

        Long randomNumber = (long) (100000 + new Random().nextInt(900000));
        
        Customer customer = new Customer();
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

}
