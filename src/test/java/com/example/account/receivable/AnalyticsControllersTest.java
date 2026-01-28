package com.example.account.receivable;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.example.account.receivable.AgingReports.Controller.AgingReportController;
import com.example.account.receivable.AgingReports.DTO.AgingReportResponse;
import com.example.account.receivable.AgingReports.Service.AgingReportService;
import com.example.account.receivable.ArCalculation.Controller.ArCalculationController;
import com.example.account.receivable.ArCalculation.DTO.CompanyBalanceSeriesDto;
import com.example.account.receivable.ArCalculation.DTO.CompanyBalanceSeriesDto.Point;
import com.example.account.receivable.ArCalculation.DTO.CompanyMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.DTO.CustomerMonthEndBalanceDto;
import com.example.account.receivable.ArCalculation.Service.CompanyBalanceCalculator;
import com.example.account.receivable.ArCalculation.Service.CustomerBalanceCalculator;
import com.example.account.receivable.Dashboard.Controller.DashboardController;
import com.example.account.receivable.Dashboard.DTO.DashboardSummaryResponse;
import com.example.account.receivable.Dashboard.Service.DashboardService;

@WebMvcTest({AgingReportController.class, ArCalculationController.class, DashboardController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({AnalyticsControllersTest.TestConfig.class, ControllerTestSecurityConfig.class})
class AnalyticsControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AgingReportService agingReportService;

    @Autowired
    private CompanyBalanceCalculator companyBalanceCalculator;

    @Autowired
    private CustomerBalanceCalculator customerBalanceCalculator;

    @Autowired
    private DashboardService dashboardService;

    @Test
    void getAgingReport_returnsResponse() throws Exception {
        LocalDate asOfDate = LocalDate.now();
        AgingReportResponse response = new AgingReportResponse(asOfDate, List.of());
        when(agingReportService.getAgingReport(1L, asOfDate, 2L, "OPEN")).thenReturn(response);

        mockMvc.perform(
                get("/reports/aging/company/{companyId}", 1L)
                        .param("customerId", "2")
                        .param("status", "OPEN")
                        .param("asOfDate", asOfDate.toString())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.asOfDate").value(asOfDate.toString()));

        verify(agingReportService).getAgingReport(1L, asOfDate, 2L, "OPEN");
    }

    @Test
    void getCompanyMonthEnd_returnsSeriesPoint() throws Exception {
        YearMonth month = YearMonth.of(2025, 1);
        CompanyMonthEndBalanceDto dto = new CompanyMonthEndBalanceDto(5L, month, BigDecimal.TEN);
        when(companyBalanceCalculator.calculate(5L, month)).thenReturn(dto);

        mockMvc.perform(
                get("/ar/company/month-end")
                        .param("companyId", "5")
                        .param("month", month.toString())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(5))
                .andExpect(jsonPath("$.data.month").value(month.toString()))
                .andExpect(jsonPath("$.data.monthEndBalance").value(10));

        verify(companyBalanceCalculator).calculate(5L, month);
    }

    @Test
    void getCustomerMonthEnd_returnsSummary() throws Exception {
        YearMonth month = YearMonth.of(2024, 12);
        CustomerMonthEndBalanceDto dto = new CustomerMonthEndBalanceDto(7L, month, BigDecimal.ONE);
        when(customerBalanceCalculator.calculate(7L, month)).thenReturn(dto);

        mockMvc.perform(
                get("/ar/customer/month-end")
                        .param("customerId", "7")
                        .param("month", month.toString())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customerId").value(7))
                .andExpect(jsonPath("$.data.month").value(month.toString()))
                .andExpect(jsonPath("$.data.monthEndBalance").value(1));

        verify(customerBalanceCalculator).calculate(7L, month);
    }

    @Test
    void getCompanyBalanceSeries_returnsGraphData() throws Exception {
        CompanyBalanceSeriesDto dto = new CompanyBalanceSeriesDto(
                9L, List.of(new Point("2025-01", BigDecimal.valueOf(50)))
        );
        when(dashboardService.getCompanyBalanceSeries(9L, 6)).thenReturn(dto);

        mockMvc.perform(
                get("/ar/company/{companyId}/balance-series", 9L)
                        .param("months", "6")
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(9))
                .andExpect(jsonPath("$.data.series[0].month").value("2025-01"))
                .andExpect(jsonPath("$.data.series[0].balance").value(50));

        verify(dashboardService).getCompanyBalanceSeries(9L, 6);
    }

    @Test
    void getDashboardSummary_returnsAggregates() throws Exception {
        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(125),
                10L,
                BigDecimal.valueOf(900),
                BigDecimal.valueOf(400),
                80L,
                20L,
                BigDecimal.valueOf(75),
                BigDecimal.valueOf(60)
        );
        when(dashboardService.getDashboardSummary(11L)).thenReturn(summary);

        mockMvc.perform(get("/dashboard/summary/company/{companyId}", 11L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCustomers").value(10))
                .andExpect(jsonPath("$.data.totalReceivables").value(900))
                .andExpect(jsonPath("$.data.overdueMoreThan30Days").value(60));

        verify(dashboardService).getDashboardSummary(11L);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        AgingReportService agingReportService() {
            return Mockito.mock(AgingReportService.class);
        }

        @Bean
        CompanyBalanceCalculator companyBalanceCalculator() {
            return Mockito.mock(CompanyBalanceCalculator.class);
        }

        @Bean
        CustomerBalanceCalculator customerBalanceCalculator() {
            return Mockito.mock(CustomerBalanceCalculator.class);
        }

        @Bean
        DashboardService dashboardService() {
            return Mockito.mock(DashboardService.class);
        }
    }
}
