package com.example.account.receivable.Customer.Dto.ResponseDTO;

import java.util.List;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.InvoiceResponseDTO;

public class CustomerResponseDTO {
    private Long id;
    private String customerName;
    private String email;
    private List<InvoiceResponseDTO> invoices;
}
