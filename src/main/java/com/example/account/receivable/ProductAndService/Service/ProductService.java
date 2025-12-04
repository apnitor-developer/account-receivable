package com.example.account.receivable.ProductAndService.Service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.account.receivable.ProductAndService.Entity.ProductAndService;
import com.example.account.receivable.ProductAndService.Repository.ProductAndServiceRepository;

@Service
public class ProductService {
    private final ProductAndServiceRepository productAndServiceRepository;

    public ProductService(
        ProductAndServiceRepository productAndServiceRepository
    ){
        this.productAndServiceRepository = productAndServiceRepository;
    }
    

    //Get all products
    public List<ProductAndService> getAllProducts(){
        return productAndServiceRepository.findByDeletedFalseAndActiveTrue();
    }
}
