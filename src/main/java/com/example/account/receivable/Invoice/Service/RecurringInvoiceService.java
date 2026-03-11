package com.example.account.receivable.Invoice.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.Invoice.Dto.InvoiceItemDto;
import com.example.account.receivable.Invoice.Dto.RecurringInvoiceTemplateDto;
import com.example.account.receivable.Invoice.Entity.RecurringInvoiceItem;
import com.example.account.receivable.Invoice.Entity.RecurringInvoiceTemplate;
import com.example.account.receivable.Invoice.Enum.RecurringFrequency;
import com.example.account.receivable.Invoice.Repository.RecurringInvoiceTemplateRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecurringInvoiceService {

    private final RecurringInvoiceTemplateRepository templateRepo;
    private final CustomerRepository customerRepo;

    @Transactional
    public RecurringInvoiceTemplate createTemplate(RecurringInvoiceTemplateDto dto) {

        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        LocalDate nextDate = calculateNextDate(dto.getStartDate(), dto.getFrequency());

        RecurringInvoiceTemplate template = RecurringInvoiceTemplate.builder()
                .customer(customer)
                .startDate(dto.getStartDate())
                .nextGenerationDate(nextDate)
                .frequency(dto.getFrequency())
                .endAfter(dto.getEndAfter())
                .generatedCount(0)
                .active(true)
                .build();

        template = templateRepo.save(template);

        List<RecurringInvoiceItem> items = new ArrayList<>();

        for (InvoiceItemDto itemDto : dto.getItems()) {

            RecurringInvoiceItem item = RecurringInvoiceItem.builder()
                    .itemName(itemDto.getItemName())
                    .description(itemDto.getDescription())
                    .quantity(itemDto.getQuantity())
                    .rate(itemDto.getRate())
                    .tax(itemDto.getTax())
                    .template(template)
                    .build();

            items.add(item);
        }

        template.setItems(items);

        return templateRepo.save(template);
    }


    private LocalDate calculateNextDate(LocalDate start, RecurringFrequency frequency) {

        switch (frequency) {

            case DAILY:
                return start.plusDays(1);

            case WEEKLY:
                return start.plusWeeks(1);

            case MONTHLY:
                return start.plusMonths(1);

            case YEARLY:
                return start.plusYears(1);

            default:
                return start;
        }
    }
}
