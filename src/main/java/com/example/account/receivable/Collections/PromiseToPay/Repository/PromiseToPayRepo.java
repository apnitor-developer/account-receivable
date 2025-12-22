package com.example.account.receivable.Collections.PromiseToPay.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseStatus;
import com.example.account.receivable.Collections.PromiseToPay.Entity.PromiseToPay;

public interface PromiseToPayRepo extends JpaRepository<PromiseToPay, Long> {

    List<PromiseToPay> findByCustomerId(Long customerId);

    List<PromiseToPay> findByStatus(PromiseStatus status);

    List<PromiseToPay> findByStatusIn(List<PromiseStatus> statuses);

    List<PromiseToPay> findByPromiseDateBeforeAndStatus(LocalDate date, PromiseStatus status);


    @Query("""
    SELECT COALESCE(SUM(p.amountPromised), 0)
    FROM PromiseToPay p
    WHERE p.status IN :statuses
      AND p.promiseDate >= :today
    """)
    BigDecimal getCurrentPromiseAmount(
            @Param("statuses") List<PromiseStatus> statuses,
            @Param("today") LocalDate today
    );



  List<PromiseToPay> findByPromiseDateAndStatus(
      LocalDate promiseDate,
      PromiseStatus status
  );

  List<PromiseToPay> findByPromiseDateBeforeAndStatusIn(
      LocalDate date,
      List<PromiseStatus> statuses
  );


  //Used for to get the Company Promise to Pay 
  @Query("""
        SELECT p
        FROM PromiseToPay p
        JOIN p.customer c
        JOIN CompanyCustomers cc ON cc.customer = c
        WHERE cc.company.id = :companyId
  """)
  List<PromiseToPay> findByCompany(@Param("companyId") Long companyId);

} 
