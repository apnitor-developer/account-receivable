package com.example.account.receivable.Collections.PromiseToPay.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Collections.PromiseToPay.DTO.RequestDTO.PromiseToPayRequest;
import com.example.account.receivable.Collections.PromiseToPay.DTO.ResponseDTO.PromiseToPayResponse;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;
import com.example.account.receivable.Collections.PromiseToPay.Repository.PromiseToPayRepo;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class PromiseToPayServiceTest {

    @Mock
    private PromiseToPayRepo promiseToPayRepo;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PromiseToPayService promiseToPayService;

    @Test
    void createPromise_whenDateInPast_throwsBadRequest() {
        PromiseToPayRequest request = new PromiseToPayRequest();
        request.setCustomerId(1L);
        request.setPromiseDate(LocalDate.now().minusDays(1));
        request.setAmountPromised(BigDecimal.ONE);

        Customer customer = new Customer();
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> promiseToPayService.createPromise(request));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void createPromise_setsStatusBasedOnDate() {
        PromiseToPayRequest request = new PromiseToPayRequest();
        request.setCustomerId(1L);
        request.setPromiseDate(LocalDate.now().plusDays(2));
        request.setAmountPromised(new BigDecimal("50"));
        request.setNotes("call");

        Customer customer = new Customer();
        customer.setCustomerName("Acme");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        PromiseToPay saved = PromiseToPay.builder()
                .id(5L)
                .customer(customer)
                .amountPromised(request.getAmountPromised())
                .promiseDate(request.getPromiseDate())
                .status(PromiseStatus.PENDING)
                .notes("call")
                .build();
        when(promiseToPayRepo.save(any(PromiseToPay.class))).thenReturn(saved);

        PromiseToPayResponse response = promiseToPayService.createPromise(request);

        assertEquals(5L, response.getId());
        assertEquals("Acme", response.getCustomerName());
        assertEquals(PromiseStatus.PENDING, response.getStatus());
        verify(promiseToPayRepo).save(any(PromiseToPay.class));
    }

    @Test
    void getAllPromiseToPay_returnsMappedResponses() {
        PromiseToPay pending = PromiseToPay.builder()
                .id(1L)
                .status(PromiseStatus.PENDING)
                .amountPromised(BigDecimal.TEN)
                .promiseDate(LocalDate.now())
                .customer(customer("A"))
                .build();
        PromiseToPay broken = PromiseToPay.builder()
                .id(2L)
                .status(PromiseStatus.BROKEN)
                .amountPromised(BigDecimal.ONE)
                .promiseDate(LocalDate.now())
                .customer(customer("B"))
                .build();

        when(promiseToPayRepo.findByStatusIn(any())).thenReturn(List.of(pending, broken));

        List<PromiseToPayResponse> responses = promiseToPayService.getAllPromiseToPay();
        assertEquals(2, responses.size());
        assertEquals("A", responses.get(0).getCustomerName());
    }

    @Test
    void getPromiseToPayByCustomer_filtersStatuses() {
        Customer customer = new Customer();
        customer.setId(7L);
        customer.setCustomerName("Primary");
        when(customerRepository.findById(7L)).thenReturn(Optional.of(customer));

        PromiseToPay pending = PromiseToPay.builder()
                .id(1L)
                .status(PromiseStatus.PENDING)
                .amountPromised(BigDecimal.TEN)
                .promiseDate(LocalDate.now())
                .customer(customer)
                .build();
        PromiseToPay completed = PromiseToPay.builder()
                .id(2L)
                .status(PromiseStatus.COMPLETED)
                .amountPromised(BigDecimal.ONE)
                .promiseDate(LocalDate.now())
                .customer(customer)
                .build();

        when(promiseToPayRepo.findByCustomerId(7L)).thenReturn(List.of(pending, completed));

        List<PromiseToPayResponse> responses = promiseToPayService.getPromiseToPayByCustomer(7L);
        assertEquals(1, responses.size());
        assertEquals(PromiseStatus.PENDING, responses.get(0).getStatus());
    }

    @Test
    void getPromisesByCompany_mapsEntities() {
        PromiseToPay entity = PromiseToPay.builder()
                .id(99L)
                .status(PromiseStatus.DUE_TODAY)
                .amountPromised(BigDecimal.valueOf(20))
                .promiseDate(LocalDate.now())
                .customer(customer("ACME"))
                .notes("note")
                .build();

        when(promiseToPayRepo.findByCompany(3L)).thenReturn(List.of(entity));

        List<PromiseToPayResponse> responses = promiseToPayService.getPromisesByCompany(3L);
        assertEquals(1, responses.size());
        assertEquals("ACME", responses.get(0).getCustomerName());
    }

    private Customer customer(String name) {
        Customer customer = new Customer();
        customer.setCustomerName(name);
        return customer;
    }
}
