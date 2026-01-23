package com.example.account.receivable.Customer.Dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportTemplateMetadata {
    private String entity;
    private String format;
    private List<TemplateTab> tabs;
}

