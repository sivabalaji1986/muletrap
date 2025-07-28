package com.hbs.muletrap.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionInput {
    @NotBlank
    private String customerId;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String channel;

    @NotBlank
    private String time;

    @NotBlank
    private String country;

    @Min(0)
    private int accountAgeDays;

    private TransactionDirection direction;

    private String activitySummary;
}