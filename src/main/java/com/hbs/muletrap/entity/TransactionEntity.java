package com.hbs.muletrap.entity;

import com.hbs.muletrap.dto.TransactionDirection;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "transactions", schema = "muletrapschema")
public class TransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String customerId;
    private BigDecimal amount;
    private String channel;
    private String time;
    private String country;
    private int accountAgeDays;
    private String activitySummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionDirection direction;

    /**
     * Hibernate‑vector will now handle pgvector VECTOR(768) natively.
     */
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 768)
    @Column(columnDefinition = "vector(768)")
    private float[] embedding;

    private boolean isMule;
    private LocalDateTime createdAt = LocalDateTime.now();
}