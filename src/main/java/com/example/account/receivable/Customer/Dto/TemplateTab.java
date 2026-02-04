package com.example.account.receivable.Customer.Dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateTab {
    private String tab;
    private String description;
    private List<TemplateField> fields;
}
