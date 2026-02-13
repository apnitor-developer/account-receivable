package com.example.account.receivable.PaymentTerms.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.PaymentTerms.Dto.CreatePaymentTermRequest;
import com.example.account.receivable.PaymentTerms.Dto.PaymentTermResponseDto;
import com.example.account.receivable.PaymentTerms.Dto.UpdatePaymentTermRequest;
import com.example.account.receivable.PaymentTerms.Entity.PaymentTerm;
import com.example.account.receivable.PaymentTerms.Repository.PaymentTermRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentTermService {

    private final PaymentTermRepository repository;
    private final CompanyRepository companyRepository;

    // =========================
    // CREATE
    // =========================
    public PaymentTermResponseDto createPaymentTerm(
            Long companyId,
            CreatePaymentTermRequest request) {

        validateNameAndDays(request.getName(), request.getNetDays());

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));

        if (repository.existsByNameIgnoreCaseAndCompanyId(
                request.getName().trim(), companyId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment term already exists for this company");
        }

        PaymentTerm term = PaymentTerm.builder()
                .name(request.getName().trim())
                .netDays(request.getNetDays())
                .active(true)
                .systemDefined(false)
                .company(company)
                .build();

        return mapToDto(repository.save(term));
    }

    // =========================
    // GET ALL (Company Scoped)
    // =========================
    public List<PaymentTermResponseDto> getAllPaymentTerms(Long companyId) {

        return repository.findByCompanyIdAndActiveTrue(companyId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // =========================
    // GET BY ID
    // =========================
    public PaymentTermResponseDto getPaymentTermById(
            Long companyId,
            Long id) {

        PaymentTerm term = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment term not found"));

        return mapToDto(term);
    }

    // =========================
    // UPDATE
    // =========================
    public PaymentTermResponseDto updatePaymentTerm(
            Long companyId,
            Long id,
            UpdatePaymentTermRequest request) {

        PaymentTerm existing = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment term not found"));

        // Protect system-defined name
        if (existing.getSystemDefined() && request.getName() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "System payment terms cannot change name");
        }

        if (request.getName() != null) {

            String newName = request.getName().trim();

            if (!newName.equalsIgnoreCase(existing.getName())
                    && repository.existsByNameIgnoreCaseAndCompanyId(newName, companyId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Payment term with this name already exists");
            }

            existing.setName(newName);
        }

        if (request.getNetDays() != null) {

            if (request.getNetDays() < 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Net days must be 0 or greater");
            }

            existing.setNetDays(request.getNetDays());
        }

        if (request.getActive() != null) {
            existing.setActive(request.getActive());
        }

        return mapToDto(repository.save(existing));
    }

    // =========================
    // DELETE (Soft Delete)
    // =========================
    public void deletePaymentTerm(Long companyId, Long id) {

        PaymentTerm existing = repository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment term not found"));

        if (existing.getSystemDefined()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Default payment terms cannot be deleted");
        }

        existing.setActive(false);
        repository.save(existing);
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    private void validateNameAndDays(String name, Integer days) {

        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment term name is required");
        }

        if (days == null || days < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Net days must be 0 or greater");
        }
    }

    private PaymentTermResponseDto mapToDto(PaymentTerm term) {
        return PaymentTermResponseDto.builder()
                .id(term.getId())
                .name(term.getName())
                .netDays(term.getNetDays())
                .active(term.getActive())
                .systemDefined(term.getSystemDefined())
                .build();
    }


    // Get Global Terms
    public List<PaymentTermResponseDto> getGlobalPaymentTerms() {

    return repository.findByCompanyIsNullAndActiveTrue()
            .stream()
            .map(this::mapToDto)
            .toList();
}
}
