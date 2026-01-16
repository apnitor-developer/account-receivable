package com.example.account.receivable.ArGlMapping.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.ArGlMapping.Entity.ArGlMapping;

public interface ArGlMappingRepository extends JpaRepository<ArGlMapping, Long> {

    // Check active mapping
    boolean existsByArCode_IdAndIsActiveTrue(Long arCodeId);

    // Overlap check
    @Query("""
        SELECT COUNT(m) > 0 FROM ArGlMapping m
        WHERE m.arCode.id = :arCodeId
          AND (
            (:start <= COALESCE(m.effectiveTo, :start))
            AND (:end IS NULL OR m.effectiveFrom <= :end)
          )
    """)
    boolean hasDateOverlap(
        @Param("arCodeId") Long arCodeId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    // Get active mapping for posting
    @Query("""
        SELECT m FROM ArGlMapping m
        WHERE m.arCode.id = :arCodeId
          AND m.isActive = true
          AND m.effectiveFrom <= :date
          AND (m.effectiveTo IS NULL OR m.effectiveTo >= :date)
    """)
    Optional<ArGlMapping> findActiveMapping(
        @Param("arCodeId") Long arCodeId,
        @Param("date") LocalDate date
    );


    @Query("""
        SELECT COUNT(m) > 0 FROM ArGlMapping m
        WHERE m.arCode.id = :arCodeId
        AND m.isActive = true
    """)
    boolean hasActiveMapping(
        @Param("arCodeId") Long arCodeId
    );


    // Custom query to find active AR → GL mappings by AR Code ID
    @Query("SELECT m FROM ArGlMapping m WHERE m.arCode.id = :arCodeId AND (m.effectiveTo IS NULL OR m.effectiveTo >= CURRENT_DATE) AND m.isActive = true")
    List<ArGlMapping> findByArCodeIdAndActiveMappings(@Param("arCodeId") Long arCodeId);



    @Query("SELECT m FROM ArGlMapping m WHERE m.arCode.id = :arCodeId AND m.company.id = :companyId AND m.isActive = true")
    Optional<ArGlMapping> findActiveMappingByArCodeIdAndCompanyId(@Param("arCodeId") Long arCodeId, @Param("companyId") Long companyId);
}

