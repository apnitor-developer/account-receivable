package com.example.account.receivable.Customer.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Dto.CustomerDTO.CustomerDTO;
import com.example.account.receivable.Customer.Entity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CompanyCustomerRepository;
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

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerAddressRepository customerAddressRepository;
    @Mock
    private CustomerCashApplicationRepository customerCashApplicationRepository;
    @Mock
    private CustomerStatementRepository customerStatementRepository;
    @Mock
    private CustomerEftRepository customerEftRepository;
    @Mock
    private CustomerVatRepository customerVatRepository;
    @Mock
    private CustomerDunningCreditSettingsRepository customerDunningCreditSettingsRepository;
    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CompanyCustomerRepository companyCustomerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_whenEmailExists_throwsDuplicateException() {
        Company company = Company.builder().id(12L).companyCustomers(new ArrayList<>()).build();
        when(companyRepository.findById(12L)).thenReturn(Optional.of(company));
        when(customerRepository.findByEmail("dup@example.com")).thenReturn(Optional.of(new Customer()));

        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerName("Dup");
        dto.setEmail("dup@example.com");

        assertThrows(DuplicateCustomerException.class,
                () -> customerService.createCustomer(8L, 12L, dto));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_savesCustomerAndCompanyLink() {
        Company company = Company.builder().id(77L).companyCustomers(new ArrayList<>()).build();
        when(companyRepository.findById(77L)).thenReturn(Optional.of(company));
        when(customerRepository.findByEmail("client@example.com")).thenReturn(Optional.empty());

        Customer saved = new Customer();
        saved.setId(100L);
        saved.setCompanyCompanies(new ArrayList<>());

        when(customerRepository.save(any(Customer.class))).thenReturn(saved);
        when(companyCustomerRepository.save(any(CompanyCustomers.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDTO dto = new CustomerDTO();
        dto.setCustomerName("Client");
        dto.setEmail("client@example.com");
        dto.setCustomerType("BUSINESS");

        Customer result = customerService.createCustomer(5L, 77L, dto);

        assertEquals(saved, result);
        assertEquals(1, company.getCompanyCustomers().size());
        assertEquals(1, saved.getCompanyCompanies().size());
        verify(companyCustomerRepository).save(any(CompanyCustomers.class));
    }

    @Test
    void softDeleteCustomer_marksRecordsDeleted() {
        Customer customer = new Customer();
        customer.setId(33L);
        customer.setDeleted(false);

        Invoice invoice1 = Invoice.builder().id(1L).deleted(false).build();
        Invoice invoice2 = Invoice.builder().id(2L).deleted(false).build();

        when(customerRepository.findById(33L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.findByCustomerId(33L)).thenReturn(List.of(invoice1, invoice2));
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = customerService.softDeleteCustomer(33L);

        assertTrue(result.isDeleted());
        assertTrue(invoice1.isDeleted());
        assertTrue(invoice2.isDeleted());
        verify(invoiceRepository).saveAll(eq(List.of(invoice1, invoice2)));
    }

    @Test
    void softDeleteCustomer_whenMissing_throwsNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> customerService.softDeleteCustomer(1L));
    }
}
