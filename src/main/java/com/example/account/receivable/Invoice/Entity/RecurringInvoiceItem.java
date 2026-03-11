package com.example.account.receivable.Invoice.Entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recurring_invoice_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringInvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    private String description;

    private Integer quantity;

    private BigDecimal rate;

    private BigDecimal tax;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "template_id")
    private RecurringInvoiceTemplate template;
}
