package com.example.account.receivable.Invoice.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Common.EmailService;
import com.example.account.receivable.Common.InvoiceTemplateService;
import com.example.account.receivable.Common.PdfGeneratorService;
import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.Customer.Dto.ImportTemplateMetadata;
import com.example.account.receivable.Customer.Dto.TemplateField;
import com.example.account.receivable.Customer.Dto.TemplateTab;
import com.example.account.receivable.Customer.Entity.Customer;
import com.example.account.receivable.Customer.Repository.CompanyCustomerRepository;
import com.example.account.receivable.Customer.Repository.CustomerRepository;
import com.example.account.receivable.GL.Dto.GlTransactionCreateRequest;
import com.example.account.receivable.GL.Enum.GlReferenceType;
import com.example.account.receivable.GL.Service.GlTransactionService;
import com.example.account.receivable.HelperMethods.CompanyResolver;
import com.example.account.receivable.Invoice.InvoiceAgingProjection;
import com.example.account.receivable.Invoice.InvoiceStatusProjection;
import com.example.account.receivable.Invoice.Dto.InvoiceDto;
import com.example.account.receivable.Invoice.Dto.InvoiceImportResultDto;
import com.example.account.receivable.Invoice.Dto.InvoiceItemDto;
import com.example.account.receivable.Invoice.Dto.OverdueInvoiceResponseDTO;
import com.example.account.receivable.Invoice.Dto.RowErrorDto;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.CustomerWithPendingAmountResponseDTO;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.InvoiceAgingDto;
import com.example.account.receivable.Invoice.Dto.ResponseDTO.InvoiceStatusBreakdownResponseDto;
import com.example.account.receivable.Invoice.Entity.Invoice;
import com.example.account.receivable.Invoice.Entity.InvoiceItem;
import com.example.account.receivable.Invoice.Enum.InvoiceStatus;
import com.example.account.receivable.Invoice.Repository.InvoiceItemRepo;
import com.example.account.receivable.Invoice.Repository.InvoiceRepository;
import com.example.account.receivable.Invoice.utils.CsvImportRow;
import com.example.account.receivable.Invoice.utils.ImportRow;
import com.example.account.receivable.Invoice.utils.InvoiceHeaderMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepo invoiceItemRepo;
    private final CompanyRepository companyRepository;
    private final GlTransactionService glTransactionService;
    private final CompanyCustomerRepository companyCustomerRepository;

    private static final String INVOICE_PREFIX = "INV-";
    private static final int INVOICE_NUMBER_WIDTH = 4;  // 0001 – 9999
    private final EmailService emailService;
    private final InvoiceTemplateService invoiceTemplateService;
    private final PdfGeneratorService pdfGeneratorService;

    
    public void sendInvoiceEmail(Long invoiceId , Long companyId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));

        Company company = companyRepository.findById(companyId)
                .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));


        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invoice must be approved before sending"
            );
        }

        String emailHtml = invoiceTemplateService.generateEmailHtml(invoice, company);
        String pdfHtml   = invoiceTemplateService.generatePdfHtml(invoice, company);

        byte[] pdf = pdfGeneratorService.generatePdf(pdfHtml);

        String customerEmail = invoice.getCustomer().getEmail();
        
        String subject = "Invoice " + invoice.getInvoiceNumber();

        emailService.sendWithAttachment(customerEmail, subject, emailHtml, pdf);

    }


    //Get Customer Open Invoices
    public List<Invoice> getOpenInvoices(Long customerId){

        Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        // Fetch only unpaid invoices
        List<InvoiceStatus> statuses = List.of(
                InvoiceStatus.OPEN,
                InvoiceStatus.PARTIAL
        );

        return invoiceRepository.findByCustomerIdAndStatusIn(customerId, statuses);
    }



    //Get Invoices By the CompanyId
    public Page<Invoice> getOpenAndPartialInvoicesByCompanyId(
            Long companyId,
            int page,
            int size,
            LocalDate dateFrom,
            LocalDate dateTo,
            Integer months
    ) {
        Pageable pageable = PageRequest.of(page, size);

        LocalDate resolvedFrom = dateFrom;
        LocalDate resolvedTo = dateTo;

        // If explicit dates are NOT provided, but months is provided
        if (resolvedFrom == null && resolvedTo == null && months != null && months > 0) {
            resolvedTo = LocalDate.now();
            resolvedFrom = resolvedTo.minusMonths(months);
        }

        return invoiceRepository.findCompanyInvoicesByStatusAndDateRange(
                companyId,
                pageable,
                List.of( InvoiceStatus.CREATED, InvoiceStatus.OPEN, InvoiceStatus.PARTIAL),
                resolvedFrom,
                resolvedTo
        );
    }


    // Get company customers whose balanceDue > 0
    public List<CustomerWithPendingAmountResponseDTO> getCustomersWithPendingAmountByCompany(Long companyId) {
        List<Object[]> results = customerRepository.findCustomersWithPendingAmountByCompanyId(companyId);

        List<CustomerWithPendingAmountResponseDTO> response = new ArrayList<>();

        for (Object[] row : results) {
            CustomerWithPendingAmountResponseDTO dto =
                new CustomerWithPendingAmountResponseDTO();

            dto.setId((Long) row[0]);
            dto.setCustomerName((String) row[1]);
            dto.setOverdueAmount((BigDecimal) row[2]);

            response.add(dto);
        }

        return response;
    }





    @Transactional
    public Invoice createInvoice(Long customerId, InvoiceDto dto) {
        // Validate Customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));


        // Resolve invoice number
        String invoiceNumber;
        boolean auto = Boolean.TRUE.equals(dto.getGenerated());

        if (auto) {
            invoiceNumber = generateUniqueInvoiceNumber();
        } else {
            if (dto.getInvoiceNumber() == null || dto.getInvoiceNumber().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invoice number is required when isGenerated is false"
                );
            }
            String manual = dto.getInvoiceNumber().trim();
            if (invoiceRepository.existsByInvoiceNumber(manual)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Invoice number already exists"
                );
            }
            invoiceNumber = manual;
        }


        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;


        // Create invoice (without totals yet)
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .invoiceDate(dto.getInvoiceDate())
                .dueDate(dto.getDueDate())
                .county(dto.getCounty())
                .note(dto.getNote())
                .status(InvoiceStatus.CREATED)
                .generated(dto.getGenerated())
                .customer(customer)
                .active(true)
                .deleted(false)
                .build();


        List<InvoiceItem> items = new ArrayList<>();


        for (InvoiceItemDto itemDto : dto.getItems()) {

            BigDecimal qty = BigDecimal.valueOf(itemDto.getQuantity());
            BigDecimal amount = itemDto.getRate().multiply(qty);

            // Convert tax
            BigDecimal taxPercent = BigDecimal.ZERO;

            if (itemDto.getTax() != null && !itemDto.getTax().equalsIgnoreCase("none")) {
                String clean = itemDto.getTax().replace("%", ""); // "10%" -> "10"
                taxPercent = new BigDecimal(clean);
            }

            BigDecimal taxAmount = amount.multiply(taxPercent).divide(BigDecimal.valueOf(100));
            BigDecimal total = amount.add(taxAmount);

            subTotal = subTotal.add(amount);
            taxTotal = taxTotal.add(taxAmount);

            InvoiceItem item = InvoiceItem.builder()
                    .itemName(itemDto.getItemName())
                    .description(itemDto.getDescription())
                    .quantity(itemDto.getQuantity())
                    .rate(itemDto.getRate())
                    .amount(amount)
                    .taxAmount(taxAmount)
                    .total(total)
                    .invoice(invoice)
                    .build();

            items.add(item);
        }

        // Compute TOTAL invoice values
        BigDecimal totalAmount = subTotal.add(taxTotal);

        // enforceCreditLimit(customer, totalAmount);

        invoiceItemRepo.saveAll(items);

        invoice.setSubTotal(subTotal);
        invoice.setTotalAmount(totalAmount);
        invoice.setBalanceDue(totalAmount);

        //Now save the invoice
        invoice = invoiceRepository.save(invoice);

        // Now that invoice has an ID, attach items
        for (InvoiceItem item : items) {
            item.setInvoice(invoice);
        }
        invoiceItemRepo.saveAll(items);

        Company company =
            CompanyResolver.resolveCompanyForCustomer(invoice.getCustomer());

            // System.out.println("Company : " + company);
            // System.out.println("Invoice : " + invoice);

        glTransactionService.createTransaction(
            company.getId(),
            GlTransactionCreateRequest.builder()
                .referenceType(GlReferenceType.INVOICE)
                .referenceId(invoice.getId())
                .referenceNumber(invoice.getInvoiceNumber())
                .amount(invoice.getTotalAmount())
                .transactionDate(invoice.getInvoiceDate())
                .description("Invoice " + invoice.getInvoiceNumber())
                .build()
        );


        try {
            return invoice;
        } catch (DataIntegrityViolationException ex) {
            // Extra safety if two requests race for same number
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Invoice number already exists, please try again"
            );
        }
    }


    // Approve Invoice
    @Transactional
    public Invoice approveInvoice(Long invoiceId) {

        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Invoice not found"));

        if (invoice.getStatus() != InvoiceStatus.CREATED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only CREATED invoices can be approved"
            );
        }

        invoice.setStatus(InvoiceStatus.OPEN);
        return invoiceRepository.save(invoice);
    }


    //Get CREATED Invoices
    @Transactional()
    public Page<Invoice> getDraftInvoicesByCompany(
            Long companyId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return invoiceRepository.findCompanyInvoicesByStatus(
                companyId,
                InvoiceStatus.CREATED,
                pageable
        );
    }



    //Generate invoice number
    private String generateUniqueInvoiceNumber() {
        String prefix = INVOICE_PREFIX;

        // Get the last invoice number starting with "INV-"
        var lastOpt = invoiceRepository
                .findTopByInvoiceNumberStartingWithOrderByInvoiceNumberDesc(prefix);

        int nextNumber = 1; // default if none exist

        if (lastOpt.isPresent()) {
            String lastNumber = lastOpt.get().getInvoiceNumber(); // e.g. "INV-0042"
            String[] parts = lastNumber.split("-");
            if (parts.length == 2) {
                try {
                    int current = Integer.parseInt(parts[1]);
                    if (current >= 9999) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Maximum invoice number (INV-9999) reached"
                        );
                    }
                    nextNumber = current + 1;
                } catch (NumberFormatException ignore) {
                    // If previous value is malformed, just fall back to 1
                    nextNumber = 1;
                }
            }
        }

        // Format as 4-digit number with leading zeros
        String formatted = String.format("%0" + INVOICE_NUMBER_WIDTH + "d", nextNumber);
        String candidate = prefix + formatted; // e.g. "INV-0007"

        // Double-check uniqueness in case of manual numbers or race conditions
        int safetyCounter = 0;
        while (invoiceRepository.existsByInvoiceNumber(candidate)) {
            safetyCounter++;
            if (safetyCounter > 20) {
                // Avoid infinite loop if something weird is happening
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Unable to generate unique invoice number"
                );
            }

            nextNumber++;
            if (nextNumber > 9999) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Maximum invoice number (INV-9999) reached"
                );
            }

            formatted = String.format("%0" + INVOICE_NUMBER_WIDTH + "d", nextNumber);
            candidate = prefix + formatted;
        }

        return candidate;
    }

    //Check the credit Limit of the Customer
    // private void enforceCreditLimit(Customer customer, BigDecimal newInvoiceAmount) {
    //     CustomerDunningCreditSettings dunning = customer.getDunning();
    //     if (dunning == null) {
    //         return;
    //     }

    //     Double limitValue = dunning.getCreditLimit();
    //     if (limitValue == null) {
    //         return;
    //     }

    //     BigDecimal creditLimit = BigDecimal.valueOf(limitValue);
    //     if (creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
    //         return;
    //     }

    //     BigDecimal outstanding = invoiceRepository.getCustomerOutstandingBalance(customer.getId() , List.of(InvoiceStatus.OPEN, InvoiceStatus.PARTIAL));
    //     if (outstanding == null) {
    //         outstanding = BigDecimal.ZERO;
    //     }

    //     BigDecimal projectedExposure = outstanding.add(newInvoiceAmount);
    //     if (projectedExposure.compareTo(creditLimit) > 0) {
    //         String msg = String.format(
    //                 "Credit limit exceeded. Limit: %s",
    //                 creditLimit.toPlainString(),
    //                 projectedExposure.toPlainString()
    //         );
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    //     }
    // }

    // Get all Invoices
    public Page<Invoice> getAllInvoices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findByDeletedFalse(pageable);
    }

    // Get Single customer invoice
    public List<Invoice> getSingleCustomerInvoice(Long customerId) {
        return invoiceRepository.findByCustomerIdAndDeletedFalseAndStatusNotIn(
            customerId,
            List.of(
                InvoiceStatus.CREATED,
                InvoiceStatus.WRITTEN_OFF,
                InvoiceStatus.PAID
            )

        );
    }

    // Invoice By Id
    public Invoice getInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found with this Id"));
        return invoice;
    }



    //Get Total Pending Amount of all the invoices.
    public BigDecimal getCustomerPendingAmount(Long customerId) {

        customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        List<Invoice> invoices =
                invoiceRepository.findByCustomerIdAndBalanceDueGreaterThan(
                        customerId, BigDecimal.ZERO
                );

        LocalDate today = LocalDate.now();

        return invoices.stream()
                .filter(inv -> inv.getDueDate() != null &&
                            inv.getDueDate().isBefore(today))
                .map(Invoice::getBalanceDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    //Calculate Total Pending Amount of the Company
    public BigDecimal getCompanyPendingAmount(Long companyId) {

        // Validate company exists (optional but recommended)
        companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"
                ));

        return invoiceRepository.getCompanyTotalPendingAmount(companyId);
    }



    // Get all Overdue Invoices of the company and their balance is greater than 0
    public List<OverdueInvoiceResponseDTO> getOverdueInvoicesByCompany(Long companyId) {
        List<Object[]> rows =
            invoiceRepository.findOverdueInvoicesByCompany(companyId , List.of( InvoiceStatus.OPEN, InvoiceStatus.PARTIAL));

        List<OverdueInvoiceResponseDTO> response = new ArrayList<>();

        for (Object[] row : rows) {
            OverdueInvoiceResponseDTO dto = new OverdueInvoiceResponseDTO();

            dto.setInvoiceId((Long) row[0]);
            dto.setInvoiceNumber((String) row[1]);
            dto.setInvoiceDate((LocalDate) row[2]);
            dto.setDueDate((LocalDate) row[3]);
            dto.setStatus((String) row[4]);
            dto.setTotalAmount((BigDecimal) row[5]);
            dto.setBalanceDue((BigDecimal) row[6]);
            dto.setCustomerId((Long) row[7]);
            dto.setCustomerName((String) row[8]);

            response.add(dto);
        }

        return response;
    }

    // Get All invoices of the company based on the filters
    public Page<Invoice> getCompanyInvoices(
            Long companyId,
            List<InvoiceStatus> statuses,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        if (statuses != null && statuses.isEmpty()) {
            statuses = null;
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fromDate cannot be after toDate"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "invoiceDate")
        );

        return invoiceRepository.findCompanyInvoicesFiltered(
                companyId,
                statuses,
                fromDate,
                toDate,
                pageable
        );
    }



    //Calculate Invoice Reports(CURRENT , 0-30 , 30-60 , 60-90 , 90>)
    public InvoiceAgingDto getInvoiceAging(Long companyId) {
        LocalDate today = LocalDate.now();

        InvoiceAgingProjection p =
            invoiceRepository.getInvoiceAgingReport(
                companyId,
                List.of( InvoiceStatus.OPEN, InvoiceStatus.PARTIAL),
                today,
                today.minusDays(30),
                today.minusDays(60),
                today.minusDays(90)
            );

        return new InvoiceAgingDto(
            p.getCurrent(),
            p.getDays0to30(),
            p.getDays31to60(),
            p.getDays61to90(),
            p.getDays90Plus()
        );
    }


    //Calculate Invoice Report(OPEN ,PARTIAL , PAID , WRITTEN_OFF)
    public InvoiceStatusBreakdownResponseDto getInvoiceStatusBreakdown(
        Long companyId,
        int months
    ) {
        LocalDate fromDate = LocalDate.now().minusMonths(months);

        InvoiceStatusProjection p =
                invoiceRepository.getInvoiceStatusBreakdown(companyId, fromDate);

        long open = p.getOpen() == null ? 0 : p.getOpen();
        long partial = p.getPartial() == null ? 0 : p.getPartial();
        long paid = p.getPaid() == null ? 0 : p.getPaid();
        long writtenOff = p.getWrittenOff() == null ? 0 : p.getWrittenOff();

        long total = open + partial + paid + writtenOff;

        return new InvoiceStatusBreakdownResponseDto(
                open,
                partial,
                paid,
                writtenOff,
                total
        );
    }




    // Invoice Template 
    public ImportTemplateMetadata getTemplateMetadata() {

        return ImportTemplateMetadata.builder()
                .entity("Invoice")
                .format("CSV, XLS, XLSX")
                .tabs(List.of(

                        // ================= INVOICE TAB =================
                        TemplateTab.builder()
                                .tab("Invoice")
                                .description(
                                        "Each row represents ONE invoice item. " +
                                        "To create an invoice with multiple items, " +
                                        "repeat the same invoice details (customerEmail, invoiceNumber, invoiceDate, dueDate) " +
                                        "across multiple rows and change only the item fields."
                                )
                                .fields(List.of(
                                        fieldWithExample(
                                                "customerEmail",
                                                "Customer Email",
                                                true,
                                                "email",
                                                null,
                                                "demo.customer@company.com"
                                        ),

                                        fieldWithExample(
                                                "isGenerated",
                                                "Auto Generate Invoice Number",
                                                true,
                                                "boolean",
                                                null,
                                                "false"
                                        ),

                                        fieldWithExample(
                                                "invoiceNumber",
                                                "Invoice Number",
                                                false,
                                                "string",
                                                Map.of("maxLength", 32),
                                                "INV-9200"
                                        ),

                                        fieldWithExample(
                                                "invoiceDate",
                                                "Invoice Date",
                                                true,
                                                "date",
                                                null,
                                                "2025-02-01"
                                        ),

                                        fieldWithExample(
                                                "dueDate",
                                                "Due Date",
                                                true,
                                                "date",
                                                null,
                                                "2025-02-15"
                                        ),

                                        fieldWithExample(
                                                "note",
                                                "Note",
                                                false,
                                                "string",
                                                null,
                                                "Website development project"
                                        )
                                ))
                                .build(),

                        // ================= ITEMS TAB =================
                        TemplateTab.builder()
                                .tab("Items")
                                .description(
                                        "Add ONE item per row. " +
                                        "For multiple items in the same invoice, " +
                                        "repeat the invoice details and change item fields only."
                                )
                                .fields(List.of(
                                        fieldWithExample(
                                                "itemName",
                                                "Item Name",
                                                true,
                                                "string",
                                                null,
                                                "Frontend Development"
                                        ),

                                        fieldWithExample(
                                                "description",
                                                "Description",
                                                false,
                                                "string",
                                                null,
                                                "React UI implementation"
                                        ),

                                        fieldWithExample(
                                                "quantity",
                                                "Quantity",
                                                true,
                                                "number",
                                                Map.of("min", 1),
                                                "2"
                                        ),

                                        fieldWithExample(
                                                "rate",
                                                "Rate",
                                                true,
                                                "number",
                                                Map.of("min", 0),
                                                "500"
                                        ),

                                        fieldWithExample(
                                                "tax",
                                                "Tax %",
                                                false,
                                                "string",
                                                Map.of("pattern", "percentage_or_none"),
                                                "10%"
                                        )
                                ))
                                .build()
                ))
                .build();
    }


    // Helper Method
    private TemplateField fieldWithExample(
            String name,
            String label,
            boolean required,
            String type,
            Map<String, Object> rules,
            String example
    ) {
        Map<String, Object> finalRules = new HashMap<>();
        if (rules != null) {
            finalRules.putAll(rules);
        }
        finalRules.put("example", example);

        return TemplateField.builder()
                .name(name)
                .label(label)
                .required(required)
                .type(type)
                .rules(finalRules)
                .build();
    }




    @Transactional
    public InvoiceImportResultDto importInvoices(Long companyId, MultipartFile file) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Company not found"
                        ));

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only CSV files are supported. Please upload CSV."
            );
        }

        List<RowErrorDto> errors = new ArrayList<>();
        int total = importCsv(company, file, errors);

        return InvoiceImportResultDto.builder()
                .totalRows(total)
                .successCount(total - errors.size())
                .failureCount(errors.size())
                .errors(errors)
                .build();
    }



    private int importCsv(
            Company company,
            MultipartFile file,
            List<RowErrorDto> errors
    ) {
        int totalRows = 0;

        try (Reader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CSVParser parser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim()
                    .parse(reader);

            InvoiceHeaderMapper headerMapper =
                    new InvoiceHeaderMapper(parser.getHeaderMap().keySet());

            Map<String, List<ImportRow>> invoiceGroups = new LinkedHashMap<>();

            for (CSVRecord record : parser) {
                totalRows++;
                ImportRow row = new CsvImportRow(record);

                String email = headerMapper.get(row, "customerEmail");
                String invoiceNumber = headerMapper.get(row, "invoiceNumber");

                if (email == null) {
                    errors.add(RowErrorDto.builder()
                            .rowNumber(row.getRowNumber())
                            .message("customerEmail is required")
                            .build());
                    continue;
                }

                String key = email + "::" + (invoiceNumber != null ? invoiceNumber : "AUTO");
                invoiceGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(row);
            }

            for (List<ImportRow> rows : invoiceGroups.values()) {
                importInvoiceWithItems(company, rows, headerMapper);
            }

        } catch (ResponseStatusException ex) {
            // ✅ BUSINESS ERRORS → PASS THROUGH
            throw ex;

        } catch (Exception ex) {
            // ✅ ONLY REAL CSV / IO / PARSING ERRORS
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid CSV file",
                    ex
            );
        }

        return totalRows;
    }




    private void importInvoiceWithItems(
        Company company,
        List<ImportRow> rows,
        InvoiceHeaderMapper headers
    ) {
        ImportRow first = rows.get(0);

        String email = headers.get(first, "customerEmail");

        Customer customer =
                companyCustomerRepository
                        .findCustomerByCompanyIdAndEmail(company.getId(), email)
                        .orElseThrow(() ->
                            new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Customer not found: " + email
                            )
                        );

        InvoiceDto dto = new InvoiceDto();
        dto.setGenerated(Boolean.parseBoolean(headers.get(first, "isGenerated")));
        dto.setInvoiceNumber(headers.get(first, "invoiceNumber"));
        dto.setInvoiceDate(LocalDate.parse(headers.get(first, "invoiceDate")));
        dto.setDueDate(LocalDate.parse(headers.get(first, "dueDate")));
        dto.setNote(headers.get(first, "note"));

        List<InvoiceItemDto> items = new ArrayList<>();

        for (ImportRow row : rows) {
            InvoiceItemDto item = new InvoiceItemDto();
            item.setItemName(headers.get(row, "itemName"));
            item.setQuantity(Integer.parseInt(headers.get(row, "quantity")));
            item.setRate(new BigDecimal(headers.get(row, "rate")));
            item.setTax(headers.get(row, "tax"));
            items.add(item);
        }

        dto.setItems(items);

        createInvoice(customer.getId(), dto);
    }

}