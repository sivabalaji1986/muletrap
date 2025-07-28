package com.hbs.muletrap.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {
    private UUID id;
    private BigDecimal amount;
    private String channel;
    private String time;
    private String country;
    private int accountAgeDays;
    private TransactionDirection direction;
    private String activitySummary;
    private boolean mule;
    private LocalDateTime createdAt;
}