package com.example.account.receivable.Company.Dto;
import lombok.Data;

@Data
public class CompanyProfileRequest {
    private String legalName;
    private String tradeName;
    private String companyCode;
    private String country;
    private String baseCurrency;
    private String timeZone;
}

