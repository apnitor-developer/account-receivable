package com.example.sqlserver.Dto;


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

