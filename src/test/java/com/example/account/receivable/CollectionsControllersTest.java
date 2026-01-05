package com.example.account.receivable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.Collections.Dispute.Controller.DisputeController;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeCodeResponseDto;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeDTORequest;
import com.example.account.receivable.Collections.Dispute.DTO.DisputeStatusUpdateRequest;
import com.example.account.receivable.Collections.Dispute.Entity.Dispute;
import com.example.account.receivable.Collections.Dispute.Enum.DisputeCode;
import com.example.account.receivable.Collections.Dispute.Enum.DisputeStatus;
import com.example.account.receivable.Collections.Dispute.Service.DisputeService;
import com.example.account.receivable.Collections.PromiseToPay.Controller.PromiseToPayController;
import com.example.account.receivable.Collections.PromiseToPay.DTO.RequestDTO.PromiseToPayRequest;
import com.example.account.receivable.Collections.PromiseToPay.DTO.ResponseDTO.PromiseToPayResponse;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Service.PromiseToPayService;
import com.example.account.receivable.Collections.Reminder.Controller.ReminderController;
import com.example.account.receivable.Collections.Reminder.Service.ReminderService;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest({DisputeController.class, PromiseToPayController.class, ReminderController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({CollectionsControllersTest.TestConfig.class, ControllerTestSecurityConfig.class})
class CollectionsControllersTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private DisputeService disputeService;

    @Autowired
    private PromiseToPayService promiseToPayService;

    @Autowired
    private ReminderService reminderService;

    @Test
    void createDispute_returnsCreatedEntity() throws Exception {
        DisputeDTORequest request = new DisputeDTORequest();
        request.setCustomerId(4L);
        request.setInvoiceId(8L);
        request.setDisputeCode(DisputeCode.PRICE.name());
        request.setDisputedAmount("150.00");
        request.setReason("Incorrect line item");

        Dispute dispute = Dispute.builder()
                .id(1L)
                .disputeId("DSP-0001")
                .reason("Incorrect line item")
                .disputedAmount(BigDecimal.valueOf(150))
                .status(DisputeStatus.OPEN)
                .build();

        when(disputeService.createDispute(any(DisputeDTORequest.class))).thenReturn(dispute);

        mockMvc.perform(
                post("/api/disputes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.disputeId").value("DSP-0001"));

        verify(disputeService).createDispute(any(DisputeDTORequest.class));
    }

    @Test
    void getDisputeById_returnsData() throws Exception {
        Dispute dispute = Dispute.builder()
                .id(3L)
                .disputeId("DSP-1010")
                .reason("test")
                .build();

        when(disputeService.getByDisputeId(3L)).thenReturn(dispute);

        mockMvc.perform(get("/api/disputes/{disputeId}", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.disputeId").value("DSP-1010"));

        verify(disputeService).getByDisputeId(3L);
    }

    @Test
    void getDisputeCodes_returnsAllCodes() throws Exception {
        List<DisputeCodeResponseDto> codes = List.of(
                new DisputeCodeResponseDto("BR", "Billing Error"),
                new DisputeCodeResponseDto("PD", "Product Defect")
        );
        when(disputeService.getDisputeCodes()).thenReturn(codes);

        mockMvc.perform(get("/api/disputes/codes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("BR"))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(disputeService).getDisputeCodes();
    }

    @Test
    void getCompanyDisputeList_returnsItems() throws Exception {
        Dispute dispute = Dispute.builder()
                .id(4L)
                .disputeId("DSP-2222")
                .build();
        when(disputeService.getCompanyDisputeList(9L)).thenReturn(List.of(dispute));

        mockMvc.perform(get("/api/disputes/company/{companyId}", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].disputeId").value("DSP-2222"));

        verify(disputeService).getCompanyDisputeList(9L);
    }

    @Test
    void updateDisputeStatus_returnsUpdatedRecord() throws Exception {
        DisputeStatusUpdateRequest updateRequest = new DisputeStatusUpdateRequest("UNDER_REVIEW");
        Dispute dispute = Dispute.builder()
                .id(5L)
                .disputeId("DSP-3333")
                .status(DisputeStatus.UNDER_REVIEW)
                .build();
        when(disputeService.updateDisputeStatus(eq(5L), any(DisputeStatusUpdateRequest.class))).thenReturn(dispute);

        mockMvc.perform(
                patch("/api/disputes/{id}/status", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UNDER_REVIEW"));

        verify(disputeService).updateDisputeStatus(eq(5L), any(DisputeStatusUpdateRequest.class));
    }

    @Test
    void createPromise_returnsResponse() throws Exception {
        PromiseToPayRequest request = new PromiseToPayRequest();
        request.setCustomerId(2L);
        request.setAmountPromised(BigDecimal.valueOf(500));
        request.setPromiseDate(LocalDate.now().plusDays(3));
        request.setNotes("call customer");

        PromiseToPayResponse response = new PromiseToPayResponse(
                1L,
                "Acme",
                BigDecimal.valueOf(500),
                request.getPromiseDate(),
                PromiseStatus.PENDING,
                "call customer"
        );
        when(promiseToPayService.createPromise(any(PromiseToPayRequest.class))).thenReturn(response);

        mockMvc.perform(
                post("/collections/promise")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.customerName").value("Acme"));

        verify(promiseToPayService).createPromise(any(PromiseToPayRequest.class));
    }

    @Test
    void getAllPromises_returnsList() throws Exception {
        PromiseToPayResponse response = new PromiseToPayResponse(
                4L,
                "Acme",
                BigDecimal.valueOf(100),
                LocalDate.now(),
                PromiseStatus.DUE_TODAY,
                "note"
        );
        when(promiseToPayService.getAllPromiseToPay()).thenReturn(List.of(response));

        mockMvc.perform(get("/collections/promise"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(4));

        verify(promiseToPayService).getAllPromiseToPay();
    }

    @Test
    void getCompanyPromises_returnsCompanyList() throws Exception {
        when(promiseToPayService.getPromisesByCompany(11L))
                .thenReturn(List.of(new PromiseToPayResponse(2L, "Globex", BigDecimal.ONE, LocalDate.now(), PromiseStatus.PENDING, null)));

        mockMvc.perform(get("/collections/promise/company/{companyId}", 11L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].customerName").value("Globex"));

        verify(promiseToPayService).getPromisesByCompany(11L);
    }

    @Test
    void getPromisesByCustomer_returnsCustomerData() throws Exception {
        when(promiseToPayService.getPromiseToPayByCustomer(7L))
                .thenReturn(List.of(new PromiseToPayResponse(1L, "Wayne", BigDecimal.TEN, LocalDate.now(), PromiseStatus.BROKEN, null)));

        mockMvc.perform(get("/collections/promise/customer/{customerId}", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("BROKEN"));

        verify(promiseToPayService).getPromiseToPayByCustomer(7L);
    }

    @Test
    void sendInvoiceReminder_dispatchesToService() throws Exception {
        doNothing().when(reminderService).sendInvoiceReminder(5L);

        mockMvc.perform(post("/api/reminders/invoice/{invoiceId}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("OK"));

        verify(reminderService).sendInvoiceReminder(5L);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        DisputeService disputeService() {
            return Mockito.mock(DisputeService.class);
        }

        @Bean
        PromiseToPayService promiseToPayService() {
            return Mockito.mock(PromiseToPayService.class);
        }

        @Bean
        ReminderService reminderService() {
            return Mockito.mock(ReminderService.class);
        }
    }
}
