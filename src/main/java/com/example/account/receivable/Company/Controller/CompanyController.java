package com.example.account.receivable.Company.Controller;

import com.example.account.receivable.Company.Dto.*;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Service.CompanyService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "*") 
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;


        @PatchMapping("/{id}/update")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> patchCompany(
            @PathVariable Long id,
            @RequestBody CompanyPatchRequest request) {

        CompanyDetailsResponse details = companyService.patchCompany(id, request);

        ApiResponse<CompanyDetailsResponse> body =
                ApiResponse.<CompanyDetailsResponse>builder()
                        .status(HttpStatus.OK.value())
                        .message("Company updated successfully")
                        .data(details)
                        .build();

        return ResponseEntity.ok(body);
    }


    @PostMapping("/{id}/users")
    public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveCompanyUsers(
        @PathVariable Long id,
        @RequestBody ManageUsersRequest request) {

    companyService.upsertCompanyUsers(id, request);

    CompanyDetailsResponse details = companyService.getCompanyDetails(id);

    ApiResponse<CompanyDetailsResponse> body =
            ApiResponse.<CompanyDetailsResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Company users saved successfully")
                    .data(details)
                    .build();

    return ResponseEntity.ok(body);
}

//     @PutMapping("/{id}/users/update")
//     public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateCompanyUsers(
//             @PathVariable Long id,
//             @RequestBody ManageUsersRequest request) {

//         companyService.upsertCompanyUsers(id, request);

//         CompanyDetailsResponse details = companyService.getCompanyDetails(id);

//         ApiResponse<CompanyDetailsResponse> body =
//                 ApiResponse.<CompanyDetailsResponse>builder()
//                         .status(HttpStatus.OK.value())
//                         .message("Company users updated successfully")
//                         .data(details)
//                         .build();

//         return ResponseEntity.ok(body);
//     }

@PostMapping(
        value = "/{id}/opening-balance-file",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
public ResponseEntity<ApiResponse<OpeningBalanceFileResponse>> uploadOpeningBalanceFile(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file) {

    OpeningBalanceFileResponse resp = companyService.saveOpeningBalanceFile(id, file);

    ApiResponse<OpeningBalanceFileResponse> body =
            ApiResponse.<OpeningBalanceFileResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Opening balance CSV uploaded successfully")
                    .data(resp)
                    .build();

    return ResponseEntity.ok(body);
}

//         @PutMapping(
//             value = "/{id}/opening-balance-file/update",
//             consumes = MediaType.MULTIPART_FORM_DATA_VALUE
//     )
//     public ResponseEntity<ApiResponse<OpeningBalanceFileResponse>> updateOpeningBalanceFile(
//             @PathVariable Long id,
//             @RequestParam("file") MultipartFile file) {

//         OpeningBalanceFileResponse resp = companyService.saveOpeningBalanceFile(id, file);

//         ApiResponse<OpeningBalanceFileResponse> body =
//                 ApiResponse.<OpeningBalanceFileResponse>builder()
//                         .status(HttpStatus.OK.value())
//                         .message("Opening balance CSV updated successfully")
//                         .data(resp)
//                         .build();

//         return ResponseEntity.ok(body);
//     }


    // STEP 1 – create company
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Validated @RequestBody CompanyProfileRequest request) {

        Company company = companyService.createCompanyStep1(request);
        CompanyResponse dto = CompanyResponse.fromEntity(company);

        ApiResponse<CompanyResponse> body = ApiResponse.<CompanyResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Company created successfully")
                .data(dto)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/{id}/company-address")
public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveContactAndAddress(
        @PathVariable Long id,
        @RequestBody CompanyContactAddressRequest request) {

    Company updated = companyService.updateCompanyContactAndAddress(id, request);
    CompanyDetailsResponse details = companyService.toDetailsResponse(updated);

    ApiResponse<CompanyDetailsResponse> body =
            ApiResponse.<CompanyDetailsResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Company contact and address saved successfully")
                    .data(details)
                    .build();

    return ResponseEntity.ok(body);
}


//         @PutMapping("/{id}/update")
//     public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateCompany(
//             @PathVariable Long id,
//             @Valid @RequestBody CompanyProfileRequest request) {

//         Company updated = companyService.updateCompanyBasic(id, request);
//         CompanyDetailsResponse details = companyService.toDetailsResponse(updated);

//         ApiResponse<CompanyDetailsResponse> body =
//                 ApiResponse.<CompanyDetailsResponse>builder()
//                         .status(HttpStatus.OK.value())
//                         .message("Company updated successfully")
//                         .data(details)
//                         .build();

//         return ResponseEntity.ok(body);
//     }

    // STEP 2 – financial settings (POST)
@PostMapping("/{id}/financial-settings")
public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveFinancialSettings(
        @PathVariable Long id,
        @RequestBody FinancialSettingsRequest request) {

    companyService.upsertFinancialSettings(id, request);

    CompanyDetailsResponse details = companyService.getCompanyDetails(id);

    ApiResponse<CompanyDetailsResponse> body = ApiResponse.<CompanyDetailsResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Financial settings saved successfully")
            .data(details)
            .build();

    return ResponseEntity.ok(body);
}

//         @PutMapping("/{id}/financial-settings/update")
//     public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateFinancialSettings(
//             @PathVariable Long id,
//             @RequestBody FinancialSettingsRequest request) {

//         companyService.upsertFinancialSettings(id, request);

//         CompanyDetailsResponse details = companyService.getCompanyDetails(id);

//         ApiResponse<CompanyDetailsResponse> body = ApiResponse.<CompanyDetailsResponse>builder()
//                 .status(HttpStatus.OK.value())
//                 .message("Financial settings updated successfully")
//                 .data(details)
//                 .build();

//         return ResponseEntity.ok(body);
//     }


    // STEP 3 – banking + payment methods (POST)
@PostMapping("/{id}/banking")
public ResponseEntity<ApiResponse<CompanyDetailsResponse>> saveBanking(
        @PathVariable Long id,
        @RequestBody BankingStepRequest request) {

    companyService.upsertBankingAndPayment(id, request);

    CompanyDetailsResponse details = companyService.getCompanyDetails(id);

    ApiResponse<CompanyDetailsResponse> body = ApiResponse.<CompanyDetailsResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Banking and payment settings saved successfully")
            .data(details)
            .build();

    return ResponseEntity.ok(body);
}

//             @PutMapping("/{id}/banking/update")
//     public ResponseEntity<ApiResponse<CompanyDetailsResponse>> updateBanking(
//             @PathVariable Long id,
//             @RequestBody BankingStepRequest request) {

//         companyService.upsertBankingAndPayment(id, request);

//         CompanyDetailsResponse details = companyService.getCompanyDetails(id);

//         ApiResponse<CompanyDetailsResponse> body = ApiResponse.<CompanyDetailsResponse>builder()
//                 .status(HttpStatus.OK.value())
//                 .message("Banking and payment settings updated successfully")
//                 .data(details)
//                 .build();

//         return ResponseEntity.ok(body);
//     }


    // GET single company
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<CompanyDetailsResponse>> getCompany(@PathVariable Long id) {
    CompanyDetailsResponse details = companyService.getCompanyDetails(id);

    ApiResponse<CompanyDetailsResponse> body = ApiResponse.<CompanyDetailsResponse>builder()
            .status(HttpStatus.OK.value())
            .message("Company fetched successfully")
            .data(details)
            .build();

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
            ApiResponse.<CompanyListResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Companies fetched successfully")
                    .data(listResponse)
                    .build();

    return ResponseEntity.ok(body);
}



    // DELETE company
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);

        ApiResponse<Void> body = ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("Company deleted successfully")
                .data(null)
                .build();

        return ResponseEntity.ok(body);
    }
}
