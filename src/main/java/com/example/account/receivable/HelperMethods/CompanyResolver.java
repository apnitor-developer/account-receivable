package com.example.account.receivable.HelperMethods;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Customer.Entity.CompanyCustomers;
import com.example.account.receivable.Customer.Entity.Customer;

public final class CompanyResolver {

    private CompanyResolver() {
        // utility class
    }

    public static Company resolveCompanyForCustomer(Customer customer) {

        if (customer == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Customer is null"
            );
        }

        return customer.getCompanyCompanies()
            .stream()
            .map(CompanyCustomers::getCompany)
            .findFirst()
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer is not linked to any company"
                )
            );
    }
}

