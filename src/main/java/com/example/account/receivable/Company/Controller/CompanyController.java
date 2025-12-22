package com.example.account.receivable.Company.Controller;

import com.example.account.receivable.Company.Dto.*;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Entity.CompanyAddress;
import com.example.account.receivable.Company.Service.CompanyService;
import com.example.account.receivable.User.entity.Users;
import com.example.account.receivable.Common.ApiResponse;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

        // PATCH – partial update of company + related data
        @PatchMapping("/{id}/update")
        public ResponseEntity<ApiResponse<Company>> patchCompany(
                @PathVariable Long id,
                @RequestBody CompanyPatchRequest request) {

                Company details = companyService.patchCompany(id, request);

                ApiResponse<Company> body =
                        ApiResponse.successResponse(
                                HttpStatus.OK.value(),
                                "Company updated successfully",
                                details
                        );

                return ResponseEntity.ok(body);
        }

        
        // Invite user Users 
        @PostMapping("/{companyId}/users")
        public ResponseEntity<ApiResponse<Users>> InviteUsers(
                @PathVariable("companyId") Long companyId,
                @RequestBody CompanyUserRequest request
        ) {
                Users details = companyService.createCompanyUser(companyId , request);

                ApiResponse<Users> body =
                        ApiResponse.successResponse(
                                HttpStatus.OK.value(),
                                "Company users saved successfully",
                                details
                        );

                return ResponseEntity.ok(body);
        }



        @GetMapping("/company/users/accept")
        public ResponseEntity<Void> acceptInvite(
                @RequestParam String email 
                ) {

                companyService.validateInvite(email);

                return ResponseEntity
                        .status(HttpStatus.FOUND) // 302 redirect
                        .location(URI.create("https://7e58b8cc9552.ngrok-free.app/set-password?email=" + email))
                        .build();
        }


        // Set Password: called from set-password screen
        @PostMapping("/user/set-password")
        public ResponseEntity<ApiResponse<Void>> setPassword(@RequestBody SetPasswordDto dto) {
                companyService.acceptInvitation(dto.getEmail() , dto.getPassword());

                ApiResponse<Void> body = ApiResponse.successResponse(
                        HttpStatus.OK.value(),
                        "Password set successfully",
                        null
                );

                return ResponseEntity.ok(body);
        }


        //Get Company Users list
        @GetMapping("/users/{companyId}")
        public ResponseEntity<ApiResponse<List<Users>>> getCompanyUsers(
                @PathVariable("companyId") Long companyId
        ){
                List<Users> users = companyService.getcompanyUsers(companyId);

                ApiResponse<List<Users>> body = ApiResponse.successResponse(
                        200, 
                        "Company Users retreived successully", 
                        users);
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
        @PostMapping("/{userId}")
        public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
                @PathVariable("userId") Long userId,
                @Validated @RequestBody CompanyProfileRequest request) {

                Company company = companyService.createCompanyStep1(userId , request);
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
        public ResponseEntity<ApiResponse<CompanyAddress>> saveContactAndAddress(
                @PathVariable Long id,
                @RequestBody CompanyContactAddressRequest request) {

                CompanyAddress updated = companyService.createCompanyAddress(id, request);

                ApiResponse<CompanyAddress> body =
                        ApiResponse.successResponse(
                                HttpStatus.OK.value(),
                                "Company contact and address saved successfully",
                                updated
                        );

                return ResponseEntity.ok(body);
        }

        // STEP 2 – financial settings
        @PostMapping("/{id}/financial-settings")
        public ResponseEntity<ApiResponse<Company>> saveFinancialSettings(
                @PathVariable Long id,
                @RequestBody FinancialSettingsRequest request) {

                companyService.upsertFinancialSettings(id, request);

                Company details = companyService.getCompanyDetails(id);

                ApiResponse<Company> body =
                        ApiResponse.successResponse(
                                HttpStatus.OK.value(),
                                "Financial settings saved successfully",
                                details
                        );

                return ResponseEntity.ok(body);
        }

        // STEP 3 – banking + payment methods
        @PostMapping("/{id}/banking")
        public ResponseEntity<ApiResponse<Company>> saveBanking(
                @PathVariable Long id,
                @RequestBody BankingStepRequest request) {

                companyService.upsertBankingAndPayment(id, request);

                Company details = companyService.getCompanyDetails(id);

                ApiResponse<Company> body =
                        ApiResponse.successResponse(
                                HttpStatus.OK.value(),
                                "Banking and payment settings saved successfully",
                                details
                        );

                return ResponseEntity.ok(body);
        }

        // GET single company
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<Company>> getCompany(@PathVariable Long id) {

        Company details = companyService.getCompanyDetails(id);

        ApiResponse<Company> body = ApiResponse.successResponse(
                200,
                "Company fetched successfully",
                details
        );

        return ResponseEntity.ok(body);
        }

        
        // GET companies by user with pagination
        @GetMapping("/user/{userId}")
        public ResponseEntity<ApiResponse<Page<Company>>> getallCompany(
                @PathVariable Long userId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size
        ){
        Page<Company> detail = companyService.getAllCompanies(userId , page, size);
        ApiResponse<Page<Company>> body = ApiResponse.successResponse(
                200,
                "Company fetched successfully",
                detail
        );

        return ResponseEntity.ok(body);
        }


        // //Get company Customer
        // @GetMapping("/{companyId}/customers")
        // public ResponseEntity<ApiResponse<List<Customer>>> getCompanyCustomers(
        //         @PathVariable("companyId") Long companyId
        // ){
        //         List<Customer> customers = companyService.getCompanyCustomers(companyId);
        //         ApiResponse<List<Customer>> response = ApiResponse.successResponse(
        //                 200, 
        //                 "Customers retreived successfully", 
        //                 customers
        //         );
        //         return ResponseEntity.status(200).body(response);

        // }

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
