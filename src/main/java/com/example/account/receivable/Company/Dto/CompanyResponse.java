package com.example.account.receivable.Company.Dto;


import com.example.account.receivable.Company.Entity.Company;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {

    private Long id;
    private String legalName;
    private String tradeName;
    private String companyCode;
    private String country;
    private String baseCurrency;
    private String timeZone;

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

    public static CompanyResponse fromEntity(Company c) {
        return CompanyResponse.builder()
                .id(c.getId())
                .legalName(c.getLegalName())
                .tradeName(c.getTradeName())
                .companyCode(c.getCompanyCode())
                .country(c.getCountry())
                .baseCurrency(c.getBaseCurrency())
                .timeZone(c.getTimeZone())
                // .addressLine1(c.getAddressLine1())
                // .city(c.getCity())
                // .stateProvince(c.getStateProvince())
                // .postalCode(c.getPostalCode())
                // .addressCountry(c.getAddressCountry())
                // .primaryContactName(c.getPrimaryContactName())
                // .primaryContactEmail(c.getPrimaryContactEmail())
                // .primaryContactPhone(c.getPrimaryContactPhone())
                // .website(c.getWebsite())
                // .primaryContactCountry(c.getPrimaryContactCountry())
                .build();
    }
}

