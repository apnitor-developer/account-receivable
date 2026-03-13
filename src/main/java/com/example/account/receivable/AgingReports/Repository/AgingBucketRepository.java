package com.example.account.receivable.AgingReports.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.account.receivable.AgingReports.Entity.AgingBucket;

public interface AgingBucketRepository extends JpaRepository<AgingBucket, Long> {

    List<AgingBucket> findByCompanyIdAndActiveTrueOrderByDisplayOrderAsc(Long companyId);

}
