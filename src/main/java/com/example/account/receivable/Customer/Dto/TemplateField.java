package com.example.account.receivable.Customer.Dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TemplateField {
    private String name;              // CSV column name
    private String label;             // UI label
    private boolean required;          // true / false
    private String type;               // string, number, boolean, email
    private Map<String, Object> rules; // minLength, pattern, etc.
    private String requiredIf;         // optional conditional rule
}

