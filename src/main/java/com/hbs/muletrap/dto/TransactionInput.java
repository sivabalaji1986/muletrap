package com.hbs.muletrap.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionInput {
    private BigDecimal amount;
    private String channel;
    private String time;
    private String country;
    private int accountAgeDays;
    private String activitySummary;
}
