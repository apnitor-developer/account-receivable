package com.example.account.receivable.Company.Dto;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyListResponse {
    private List<CompanyDetailsResponse> companies;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}

