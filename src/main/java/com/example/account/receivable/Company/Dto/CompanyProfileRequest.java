package com.example.account.receivable.Company.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyProfileRequest {

    @NotBlank
    private String legalName;

    private String tradeName;
    private String companyCode;
    private String country;
    private String baseCurrency;
    private String timeZone;

    // private String addressLine1;
    // private String city;
    // private String stateProvince;
    // private String postalCode;
    // private String addressCountry;

    // private String primaryContactName;

    // @Email
    // private String primaryContactEmail;

    // private String primaryContactPhone;
    // private String website;
    // private String primaryContactCountry;
}

