package com.example.account.receivable.Invoice.Entity;

import com.example.account.receivable.Customer.Entity.Customer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 32)
    private String invoiceNumber;

    private LocalDate invoiceDate;

    private LocalDate dueDate;

    private BigDecimal subTotal;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private String description;

    @Column(name = "balance_due", nullable = false)
    private BigDecimal balanceDue;  // Default value should be set to the total amount initially

    // New field for invoice status (OPEN, PARTIAL, PAID)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private String status = "OPEN";  // Initial status is OPEN

    // Optional: Track the date of the last payment
    @Column(name = "last_payment_date")
    private LocalDate lastPaymentDate;

    private String note;

    @Column(name = "is_generated")     
    private Boolean generated;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean deleted = false;

    @ManyToOne
    @JoinColumn(name = "customer_id")   
    private Customer customer;
}

