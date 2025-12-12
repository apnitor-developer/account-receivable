package com.example.account.receivable.Collections.PromiseToPay.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Invoice.Entity.Invoice;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "promise_to_pay")
public class PromiseToPay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer making the promise
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // Optional: Promise linked to a specific invoice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(nullable = false)
    private BigDecimal amountPromised;

    @Column(nullable = false)
    private LocalDate promiseDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PromiseStatus status;

    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
