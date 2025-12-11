package com.example.account.receivable.Customer.Controller;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.account.receivable.Common.ApiResponse;
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


@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Add main user fields
    @PostMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@PathVariable("companyId") Long companyId , @RequestBody CustomerDTO customerDTO) {
        Customer customer = customerService.createCustomer(companyId , customerDTO);
        ApiResponse<Customer> response = ApiResponse.successResponse(
            201,
            "Customer Created Successfully", 
            customer
        );
        return ResponseEntity.status(201).body(response);
    }


    // Save Customer Address
    @PostMapping("/{customerId}/address")
    public ResponseEntity<ApiResponse<CustomerAddress>> saveAddress(@PathVariable Long customerId, @RequestBody CustomerAddress customerAddressDTO) {
        CustomerAddress address = customerService.saveCustomerAddress(customerId , customerAddressDTO);
        ApiResponse<CustomerAddress> response = ApiResponse.successResponse(
            201,
            "Customer Address add Successfully", 
            address
        );
        return ResponseEntity.status(201).body(response);
    }

    // Save CashApplication data
    @PostMapping("/{customerId}/cash-application")
    public ResponseEntity<ApiResponse<CashApplication>> saveCashApplication(@PathVariable Long customerId, @RequestBody CashApplication cashApplicationDTO) {
        CashApplication cashApplication = customerService.saveCashApplication(customerId , cashApplicationDTO);
        ApiResponse<CashApplication> response = ApiResponse.successResponse(
            201,
            "Customer Cash Application added Successfully", 
            cashApplication
        );
        return ResponseEntity.status(201).body(response);
    }



    // Save Customer Statement
    @PostMapping("/{customerId}/statement")
    public ResponseEntity<ApiResponse<CustomerStatement>> saveCustomerStatement(@PathVariable Long customerId, @RequestBody CustomerStatementDTO customerStatementDTO) {
        CustomerStatement customerStatement = customerService.saveCustomerStatement(customerId , customerStatementDTO);
        ApiResponse<CustomerStatement> response = ApiResponse.successResponse(
            201,
            "Customer Statement added Successfully", 
            customerStatement
        );
        return ResponseEntity.status(201).body(response);
    }

    // Save Customer EFT
    @PostMapping("/{customerId}/eft")
    public ResponseEntity<ApiResponse<CustomerEFT>> saveEft(@PathVariable Long customerId, @RequestBody CustomerEftDTO customerEftDTO) {
        CustomerEFT eft = customerService.saveCustomerEft(customerId , customerEftDTO);
        ApiResponse<CustomerEFT> response = ApiResponse.successResponse(
            201,
            "Customer EFT added Successfully", 
            eft
        );
        return ResponseEntity.status(201).body(response);
    }


    // Save Customer VAT
    @PostMapping("/{customerId}/vat")
    public ResponseEntity<ApiResponse<CustomerVAT>> saveVat(@PathVariable Long customerId, @RequestBody CustomerVatDTO customerVatDTO) {
        CustomerVAT vat = customerService.saveCustomerVat(customerId , customerVatDTO);
        ApiResponse<CustomerVAT> response = ApiResponse.successResponse(
            201,
            "Customer VAT added Successfully", 
            vat
        );
        return ResponseEntity.status(201).body(response);
    }


    // Save Customer Dunning & Credit Settings
    @PostMapping("/{customerId}/dunning-credit")
    public ResponseEntity<ApiResponse<CustomerDunningCreditSettings>> saveDunningCredit(@PathVariable Long customerId, @RequestBody CustomerDunningCreditSettingsDTO customerDunningCreditSettingsDTO) {
        CustomerDunningCreditSettings dunningCreditSettings = customerService.saveCustomerDunningCreditSettings(customerId , customerDunningCreditSettingsDTO);
        ApiResponse<CustomerDunningCreditSettings> response = ApiResponse.successResponse(
            201,
            "Customer Dunning and Credit added Successfully", 
            dunningCreditSettings
        );
        return ResponseEntity.status(201).body(response);
    }

    //Get Single Customer
    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getSingleCustomer(@PathVariable Long customerId) {
        CustomerResponseDTO customer = customerService.getSingleCustomer(customerId);
        ApiResponse<CustomerResponseDTO> response = ApiResponse.successResponse(
            200, 
            "Customer Retreived Successfully", 
            customer
        );
        return ResponseEntity.status(200).body(response);
    }


    // Get all customers
    @GetMapping()
    public ResponseEntity<ApiResponse<Page<Customer>>> getAllCustomers(
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size
    ) {
        Page<Customer> customers = customerService.getAllCustomers(page, size);
        ApiResponse<Page<Customer>> response = ApiResponse.successResponse(
            200,
            "Customers Retreived Successfully", 
            customers
        );
        return ResponseEntity.status(200).body(response);
    }

    //Delete user
    @DeleteMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Customer>> softDeleteCustomer(@PathVariable Long customerId) {
        Customer deletedCustomer = customerService.softDeleteCustomer(customerId);
        ApiResponse<Customer> response = ApiResponse.successResponse(
            200,
            "Customer deleted Successfully", 
            null
        );
        return ResponseEntity.status(200).body(response);
    }


    //Single API for Customer Creation.
    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<Customer>> createCompleteCustomer(
            @RequestBody CustomerFullRequestDTO request) {

        Customer savedCustomer = customerService.createCompleteCustomer(request);

        ApiResponse<Customer> response = ApiResponse.successResponse(
                201,
                "Customer & All Related Settings Created Successfully",
                savedCustomer
        );

        return ResponseEntity.status(201).body(response);
    }


    //Single API for Update Customer
    @PatchMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerFullUpdateDTO updateDTO) {

        Customer updatedCustomer = customerService.updateCustomer(customerId, updateDTO);

        ApiResponse<Customer> response = ApiResponse.successResponse(
                200,
                "Customer Updated Successfully",
                updatedCustomer
        );

        return ResponseEntity.ok(response);
    }

            @PostMapping(
            value = "/import-csv",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<CustomerCsv>> importCustomersCsv(
            @RequestParam("companyId") Long companyId,
            @RequestParam("file") MultipartFile file
    ) {
        CustomerCsv result = customerService.importCustomersFromCsv(companyId, file);

        ApiResponse<CustomerCsv> body = ApiResponse.successResponse(
                200,
                "Customer CSV processed successfully",
                result
        );

        return ResponseEntity.ok(body);
    }
}



