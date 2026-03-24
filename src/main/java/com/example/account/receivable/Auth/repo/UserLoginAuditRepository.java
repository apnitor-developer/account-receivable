package com.example.account.receivable.Auth.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.account.receivable.Auth.dto.LoginSecurityReportDTO;
import com.example.account.receivable.Auth.dto.MfaSecurityReportDTO;
import com.example.account.receivable.Auth.entity.UserLoginAudit;

public interface UserLoginAuditRepository extends JpaRepository<UserLoginAudit, Long> {

    List<UserLoginAudit> findByUserId(Long userId);

    @Query("SELECT a FROM UserLoginAudit a WHERE a.loginAt BETWEEN :start AND :end")
    List<UserLoginAudit> findByDateRange(Instant start, Instant end);


    @Query("""
        SELECT new com.example.account.receivable.Auth.dto.LoginSecurityReportDTO(
            u.id,
            u.email,
            u.firstName,
            a.loginAt,
            a.ipAddress,
            a.status
        )
        FROM UserLoginAudit a
        JOIN a.user u
        JOIN u.userCompanies uc
        WHERE uc.company.id = :companyId
        ORDER BY a.loginAt DESC
    """)
    List<LoginSecurityReportDTO> getLoginSecurityReportByCompany(Long companyId);


    @Query("""
        SELECT new com.example.account.receivable.Auth.dto.MfaSecurityReportDTO(
            CONCAT(u.firstName, ' ', u.lastName),
            u.email,
            r.name,
            CASE 
                WHEN u.mfaEnabled = true THEN 'Enabled'
                ELSE 'Disabled'
            END,
            u.mfaEnabledAt
        )
        FROM Users u
        JOIN u.userRoles ur
        JOIN ur.role r
        JOIN u.userCompanies uc
        WHERE uc.company.id = :companyId
    """)
    List<MfaSecurityReportDTO> getMfaSecurityReportByCompany(Long companyId);
}
