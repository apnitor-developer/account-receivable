package com.example.account.receivable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Controller.InvoiceController;
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Dto.OverdueInvoiceResponseDTO;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.CustomerWithPendingAmountResponseDTO;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Service.InvoiceService;
import com.example.account.receivable.Payment.Controller.PaymentController;
import com.example.account.receivable.Payment.Dto.ReceivePaymentRequest;
import com.example.account.receivable.Payment.Entity.Payment;
import com.example.account.receivable.Payment.Service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest({InvoiceController.class, PaymentController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({FinanceControllersTest.TestConfig.class, ControllerTestSecurityConfig.class})
class FinanceControllersTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private PaymentService paymentService;

    private Invoice sampleInvoice() {
        Customer customer = new Customer();
        customer.setId(4L);
        customer.setEmail("customer@example.com");

        return Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-001")
                .dueDate(LocalDate.now().minusDays(1))
                .balanceDue(BigDecimal.valueOf(120))
                .customer(customer)
                .build();
    }

    @Test
    void createInvoice_returnsCreatedEntity() throws Exception {
        InvoiceDto dto = new InvoiceDto();
        dto.setInvoiceNumber("INV-001");
        dto.setInvoiceDate(LocalDate.now());
        dto.setDueDate(LocalDate.now().plusDays(7));

        when(invoiceService.createInvoice(eq(6L), any(InvoiceDto.class))).thenReturn(sampleInvoice());

        mockMvc.perform(
                post("/invoice/{customerId}", 6L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-001"));

        verify(invoiceService).createInvoice(eq(6L), any(InvoiceDto.class));
    }

    @Test
    void sendInvoice_dispatchesEmail() throws Exception {
        mockMvc.perform(post("/invoice/send/{invoiceId}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("sent"));

        verify(invoiceService).sendInvoiceEmail(3L);
    }

    @Test
    void getOpenInvoices_returnsList() throws Exception {
        when(invoiceService.getOpenInvoices(2L)).thenReturn(List.of(sampleInvoice()));

        mockMvc.perform(get("/invoice/unpaid/{customerId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].invoiceNumber").value("INV-001"));

        verify(invoiceService).getOpenInvoices(2L);
    }


    @Test
    void getAllInvoices_returnsPagedData() throws Exception {
        Page<Invoice> page = new PageImpl<>(List.of(sampleInvoice()));
        when(invoiceService.getAllInvoices(0, 10)).thenReturn(page);

        mockMvc.perform(get("/invoice").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].invoiceNumber").value("INV-001"));

        verify(invoiceService).getAllInvoices(0, 10);
    }

    @Test
    void getSingleCustomerInvoice_returnsList() throws Exception {
        when(invoiceService.getSingleCustomerInvoice(9L)).thenReturn(List.of(sampleInvoice()));

        mockMvc.perform(get("/invoice/customer/{customerId}", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].invoiceNumber").value("INV-001"));

        verify(invoiceService).getSingleCustomerInvoice(9L);
    }

    @Test
    void getInvoice_returnsInvoice() throws Exception {
        when(invoiceService.getInvoice(4L)).thenReturn(sampleInvoice());

        mockMvc.perform(get("/invoice/{invoiceId}", 4L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-001"));

        verify(invoiceService).getInvoice(4L);
    }

    @Test
    void getCustomerPendingAmount_returnsAmount() throws Exception {
        when(invoiceService.getCustomerPendingAmount(7L)).thenReturn(BigDecimal.valueOf(250));

        mockMvc.perform(get("/invoice/{customerId}/pending-amount-customer", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(250));

        verify(invoiceService).getCustomerPendingAmount(7L);
    }

    @Test
    void getCustomersWithPendingAmountByCompany_returnsCustomers() throws Exception {
        CustomerWithPendingAmountResponseDTO dto = new CustomerWithPendingAmountResponseDTO();
        dto.setId(1L);
        dto.setCustomerName("Acme");
        dto.setOverdueAmount(BigDecimal.valueOf(300));

        when(invoiceService.getCustomersWithPendingAmountByCompany(2L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/invoice/company/{companyId}/with-pending-amounts", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].overdueAmount").value(300));

        verify(invoiceService).getCustomersWithPendingAmountByCompany(2L);
    }

    @Test
    void getCompanyPendingAmount_returnsAmount() throws Exception {
        when(invoiceService.getCompanyPendingAmount(8L)).thenReturn(BigDecimal.valueOf(500));

        mockMvc.perform(get("/invoice/{companyId}/pending-amount-company", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(500));

        verify(invoiceService).getCompanyPendingAmount(8L);
    }

    @Test
    void getOverdueInvoices_returnsDtoList() throws Exception {
        OverdueInvoiceResponseDTO dto = new OverdueInvoiceResponseDTO();
        dto.setInvoiceId(1L);
        dto.setInvoiceNumber("INV-001");

        when(invoiceService.getOverdueInvoicesByCompany(3L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/invoice/company/{companyId}/overdue-invoices", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].invoiceNumber").value("INV-001"));

        verify(invoiceService).getOverdueInvoicesByCompany(3L);
    }

    @Test
    void applyPayment_createsPayment() throws Exception {
        ReceivePaymentRequest request = new ReceivePaymentRequest();
        request.setPaymentAmount(BigDecimal.valueOf(200));
        request.setPaymentMethod("ACH");

        Payment payment = Payment.builder()
                .id(1L)
                .paymentAmount(BigDecimal.valueOf(200))
                .paymentMethod("ACH")
                .build();

        when(paymentService.applyPayment(eq(4L), any(ReceivePaymentRequest.class))).thenReturn(payment);

        mockMvc.perform(
                post("/payment/apply/{customerId}", 4L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.paymentAmount").value(200));

        verify(paymentService).applyPayment(eq(4L), any(ReceivePaymentRequest.class));
    }

    @Test
    void getAllPayments_returnsPage() throws Exception {
        Page<Payment> page = new PageImpl<>(List.of(
                Payment.builder().id(1L).paymentAmount(BigDecimal.valueOf(50)).paymentMethod("Card").build()
        ));
        when(paymentService.getAllPayments(0, 10)).thenReturn(page);

        mockMvc.perform(get("/payment").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].paymentMethod").value("Card"));

        verify(paymentService).getAllPayments(0, 10);
    }

    @Test
    void getPaymentsByCompany_returnsPagedPayments() throws Exception {
        Page<Payment> page = new PageImpl<>(List.of(
                Payment.builder().id(2L).paymentAmount(BigDecimal.valueOf(70)).paymentMethod("Wire").build()
        ));
        when(paymentService.getPaymentsByCompanyId(5L, 1, 5)).thenReturn(page);

        mockMvc.perform(get("/payment/company/{companyId}", 5L).param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].paymentMethod").value("Wire"));

        verify(paymentService).getPaymentsByCompanyId(5L, 1, 5);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        InvoiceService invoiceService() {
            return Mockito.mock(InvoiceService.class);
        }

        @Bean
        PaymentService paymentService() {
            return Mockito.mock(PaymentService.class);
        }
    }
}
