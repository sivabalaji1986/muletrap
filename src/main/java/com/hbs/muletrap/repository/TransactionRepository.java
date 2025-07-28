package com.hbs.muletrap.repository;

import com.hbs.muletrap.entity.TransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    /**
     * Fetch the last 10 mule-flagged transactions globally, ordered by most recent.
     */
    List<TransactionEntity> findTop10ByIsMuleTrueOrderByCreatedAtDesc();

    // new: top 10 mule‑flagged in a specific country
    List<TransactionEntity> findTop10ByIsMuleTrueAndCountryOrderByCreatedAtDesc(
            String country,
            Pageable pageable
    );

    /**
     * Fetch the last 10 mule-flagged transactions for the specified customer, ordered by most recent.
     */
    List<TransactionEntity> findTop10ByCustomerIdAndIsMuleTrueOrderByCreatedAtDesc(
            String customerId,
            Pageable pageable
    );

    /**
     * Fetch all transactions for a customer created after the given timestamp.
     */
    List<TransactionEntity> findByCustomerIdAndCreatedAtAfter(
            String customerId,
            LocalDateTime since
    );

}