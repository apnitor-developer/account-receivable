package com.example.sqlserver.Dto;


import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class CompanyContactAddressRequest {

    private String addressLine1;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String addressCountry;

    private String primaryContactName;

    @Email
    private String primaryContactEmail;

    private String primaryContactPhone;
    private String website;
    private String primaryContactCountry;
}

