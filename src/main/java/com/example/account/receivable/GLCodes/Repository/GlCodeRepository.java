package com.example.account.receivable.GLCodes.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.GLCodes.Entity.GlCode;
import com.example.account.receivable.GLCodes.Enum.GlAccountType;

public interface GlCodeRepository extends JpaRepository<GlCode, Long> {

    List<GlCode> findByCompanyId(Long companyId);

    Optional<GlCode> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndGlCode(Long companyId, String glCode);

    Optional<GlCode> findByCompanyIdAndAccountType(Long companyId, GlAccountType accountType);

    boolean existsByCompanyIdAndAccountTypeAndIsActiveTrue(Long companyId, GlAccountType accountType);
}
