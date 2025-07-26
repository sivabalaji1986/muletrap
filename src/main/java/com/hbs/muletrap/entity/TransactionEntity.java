package com.hbs.muletrap.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private BigDecimal amount;
    private String channel;
    private String time;
    private String country;
    private int accountAgeDays;
    private String activitySummary;

    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    private boolean isMule;
    private LocalDateTime createdAt = LocalDateTime.now();
}