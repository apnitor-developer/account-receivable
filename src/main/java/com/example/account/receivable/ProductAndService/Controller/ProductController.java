package com.example.account.receivable.ProductAndService.Controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.receivable.Common.ApiResponse;
import com.example.account.receivable.ProductAndService.Entity.ProductAndService;
import com.example.account.receivable.ProductAndService.Service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //Get All Products
    @GetMapping()
    public ResponseEntity<ApiResponse<List<ProductAndService>>> getAllProducts(){
        List<ProductAndService> products = productService.getAllProducts();
        ApiResponse<List<ProductAndService>> response = ApiResponse.successResponse(
            200,
            "Products retreived Successfully", 
            products
        );
        return ResponseEntity.status(200).body(response);
    }
}
