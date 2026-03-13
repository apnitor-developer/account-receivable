package com.example.account.receivable.LateFee.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.account.receivable.Company.Entity.Company;
import com.example.account.receivable.Company.Repository.CompanyRepository;
import com.example.account.receivable.LateFee.Entity.LateFeeRule;
import com.example.account.receivable.LateFee.Repository.LateFeeRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LateFeeRuleService {

    private final LateFeeRuleRepository lateFeeRuleRepository;
    private final CompanyRepository companyRepository;

    //Create Late Fee Rule
    public LateFeeRule createRule(Long companyId, LateFeeRule rule) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND,"Company not found"));

        // Prevent multiple rules
        if (lateFeeRuleRepository.existsByCompanyIdAndDelatedFalse(companyId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Late fee rule already exists for this company"
            );
        }

        rule.setCompany(company);
        rule.setDelated(false);

        return lateFeeRuleRepository.save(rule);
    }

    public LateFeeRule getRule(Long companyId) {
        return lateFeeRuleRepository.findByCompanyIdAndDelatedFalse(companyId)
                .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND,"Late fee rule not found"));
    }

    public List<LateFeeRule> getAllRules() {
        return lateFeeRuleRepository.findAll();
    }

    //Update Late Fee Rule
    public LateFeeRule updateRule(Long id, LateFeeRule rule) {

        LateFeeRule existing = lateFeeRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Rule not found"));

        if (rule.getGracePeriodDays() != null) {
            existing.setGracePeriodDays(rule.getGracePeriodDays());
        }

        if (rule.getLateFeePercentage() != null) {
            existing.setLateFeePercentage(rule.getLateFeePercentage());
        }

        if (rule.getMandatoryCharge() != null) {
            existing.setMandatoryCharge(rule.getMandatoryCharge());
        }

        return lateFeeRuleRepository.save(existing);
    }

    public LateFeeRule deleteRule(Long id) {

        LateFeeRule existing = lateFeeRuleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Rule not found"));

        existing.setDelated(true);
        
        return lateFeeRuleRepository.save(existing);
    }
}