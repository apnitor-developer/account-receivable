package com.example.account.receivable.Invoice.Entity;

import java.math.BigDecimal;

import com.example.account.receivable.ProductAndService.Entity.ProductAndService;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
@Entity
@Table(name = "invoice_items")
@Data
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private Integer quantity;

    private BigDecimal rate;

    private BigDecimal taxAmount;

    // Relationship: Each item belongs to an invoice
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    // Relationship: item refers to a product/service
    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductAndService product;


}

