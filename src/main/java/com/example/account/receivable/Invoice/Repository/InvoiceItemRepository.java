package com.example.account.receivable.Invoice.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.Invoice.Entity.InvoiceItem;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem , Long>{
    
}
