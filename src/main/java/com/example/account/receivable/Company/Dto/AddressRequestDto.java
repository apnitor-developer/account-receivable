package com.example.account.receivable.Company.Dto;

import lombok.Data;

@Data
public class AddressRequestDto {
    private String addressLine1;
    private String city;
    private String stateProvince;
    private String postalCode;
    private String addressCountry;

    private String primaryContactName;
    private String primaryContactEmail;
    private String primaryContactPhone;
    private String website;
    private String primaryContactCountry;
}
