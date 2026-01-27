package com.example.account.receivable.Company.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CompanyContactAddressRequest {

    private String addressLine1;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String addressCountry;

    private String primaryContactName;
    private String primaryContactEmail;
    private String position;

    @NotNull
    private String primaryContactPhone;
    
    private String website;
    private String primaryContactCountry;
    
}

