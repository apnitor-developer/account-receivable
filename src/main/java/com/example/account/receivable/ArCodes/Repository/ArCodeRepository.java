package com.example.account.receivable.ArCodes.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.ArCodes.Entity.ArCode;

public interface ArCodeRepository extends JpaRepository<ArCode, Long> {

    List<ArCode> findByCompanyIdAndIsDeletedFalse(Long companyId);

    Optional<ArCode> findByIdAndCompanyIdAndIsDeletedFalse(Long id, Long companyId);
}
