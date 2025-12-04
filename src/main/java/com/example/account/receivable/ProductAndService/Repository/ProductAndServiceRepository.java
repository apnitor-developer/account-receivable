package com.example.account.receivable.ProductAndService.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.ProductAndService.Entity.ProductAndService;

public interface ProductAndServiceRepository extends JpaRepository<ProductAndService , Long>{
    List<ProductAndService> findByDeletedFalseAndActiveTrue();
}
    

