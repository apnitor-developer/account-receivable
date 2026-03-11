package com.example.account.receivable.Invoice.Entity;

import java.time.LocalDate;
import java.util.List;

import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Enum.RecurringFrequency;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "recurring_invoice_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringInvoiceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;

    private LocalDate nextGenerationDate;

    @Enumerated(EnumType.STRING)
    private RecurringFrequency frequency;

    private Integer endAfter; // number of invoices

    private Integer generatedCount;

    @Builder.Default
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL)
    private List<RecurringInvoiceItem> items;
}
