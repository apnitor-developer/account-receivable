package com.example.account.receivable.Company.Controller;

import com.example.account.receivable.Company.Dto.*;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Service.CompanyService;
import com.example.account.receivable.Common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // PATCH – partial update of company + related data
    @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> patchCompany(
            @PathVariable Long id,
            @RequestBody CompanyPatchRequest request) {

        CompanyDetailsResponse details = companyService.patchCompany(id, request);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Company updated successfully",
                        details
                );

        return ResponseEntity.ok(body);
    }

    // STEP 4 – users
    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveCompanyUsers(
            @PathVariable Long id,
            @RequestBody ManageUsersRequest request) {

        companyService.upsertCompanyUsers(id, request);

        CompanyDetailsResponse details = companyService.getCompanyDetails(id);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Company users saved successfully",
                        details
                );

        return ResponseEntity.ok(body);
    }

    // STEP 5 – opening balance CSV upload
    @PostMapping(
            value = "/{id}/opening-balance-file",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<OpeningBalanceFileResponse>> uploadOpeningBalanceFile(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        OpeningBalanceFileResponse resp = companyService.saveOpeningBalanceFile(id, file);

        ApiResponse<OpeningBalanceFileResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Opening balance CSV uploaded successfully",
                        resp
                );

        return ResponseEntity.ok(body);
    }

    // STEP 1 – create company
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Validated @RequestBody CompanyProfileRequest request) {

        Company company = companyService.createCompanyStep1(request);
        CompanyResponse dto = CompanyResponse.fromEntity(company);

        ApiResponse<CompanyResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.CREATED.value(),
                        "Company created successfully",
                        dto
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // STEP 1 – contact + address (separate API)
    @PostMapping("/{id}/company-address")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveContactAndAddress(
            @PathVariable Long id,
            @RequestBody CompanyContactAddressRequest request) {

        Company updated = companyService.updateCompanyContactAndAddress(id, request);
        CompanyDetailsResponse details = companyService.toDetailsResponse(updated);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Company contact and address saved successfully",
                        details
                );

        return ResponseEntity.ok(body);
    }

    // STEP 2 – financial settings
    @PostMapping("/{id}/financial-settings")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveFinancialSettings(
            @PathVariable Long id,
            @RequestBody FinancialSettingsRequest request) {

        companyService.upsertFinancialSettings(id, request);

        CompanyDetailsResponse details = companyService.getCompanyDetails(id);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Financial settings saved successfully",
                        details
                );

        return ResponseEntity.ok(body);
    }

    // STEP 3 – banking + payment methods
    @PostMapping("/{id}/banking")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveBanking(
            @PathVariable Long id,
            @RequestBody BankingStepRequest request) {

        companyService.upsertBankingAndPayment(id, request);

        CompanyDetailsResponse details = companyService.getCompanyDetails(id);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Banking and payment settings saved successfully",
                        details
                );

        return ResponseEntity.ok(body);
    }

    // GET single company
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompany(@PathVariable Long id) {
        CompanyDetailsResponse details = companyService.getCompanyDetails(id);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Company fetched successfully",
                        details
                );

        return ResponseEntity.ok(body);
    }

    // GET companies with pagination
    @GetMapping
    public ResponseEntity<ApiResponse<CompanyListResponse>> listCompanies(Pageable pageable) {

        Page<Company> page = companyService.listCompanies(pageable);

        List<CompanyDetailsResponse> companies = page.getContent().stream()
                .map(companyService::toDetailsResponse)
                .toList();

        CompanyListResponse listResponse = CompanyListResponse.builder()
                .companies(companies)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();

        ApiResponse<CompanyListResponse> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Companies fetched successfully",
                        listResponse
                );

        return ResponseEntity.ok(body);
    }

    // DELETE (soft delete) company
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);

        ApiResponse<Void> body =
                ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Company deleted successfully",
                        null
                );

        return ResponseEntity.ok(body);
    }
}
