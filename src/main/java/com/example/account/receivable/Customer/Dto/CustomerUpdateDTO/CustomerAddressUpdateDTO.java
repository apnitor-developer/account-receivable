package com.example.account.receivable.Customer.Dto.CustomerUpdateDTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerAddressUpdateDTO {
    private String addressLine1;
    private String city;
    private String postalCode;
    private String country;
    private String stateProvince;
}