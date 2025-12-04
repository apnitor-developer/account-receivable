package com.example.account.receivable.Customer.Dto.CustomerDTO;

import lombok.Data;

@Data
public class CustomerAddressDTO {

    private String addressLine1;
    private String city;
    private String postalCode;
    private String country;
    private String stateProvince;
}
