package com.hbs.muletrap.repository;

import com.hbs.muletrap.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findTop10ByIsMuleTrueOrderByCreatedAtDesc();

    List<TransactionEntity> findByCreatedAtAfter(LocalDateTime after);

}