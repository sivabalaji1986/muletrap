package com.hbs.muletrap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbs.muletrap.dto.TransactionDirection;
import com.hbs.muletrap.dto.TransactionInput;
import com.hbs.muletrap.dto.TransactionResponse;
import com.hbs.muletrap.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("TransactionController (standalone)")
class TransactionControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private TransactionService svc;

    @InjectMocks
    private TransactionController ctrl;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders
                .standaloneSetup(ctrl)
                .build();
    }

    @Nested @DisplayName("POST /transactions")
    class Submit {
        @Test @DisplayName("null service result → 400")
        void nullResultGivesBadRequest() throws Exception {
            when(svc.process(any())).thenReturn(null);
            TransactionInput in = new TransactionInput();
            mockMvc.perform(post("/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(in)))
                    .andExpect(status().isBadRequest());
        }

        @Test @DisplayName("valid result → 200 + JSON")
        void validResultGivesOk() throws Exception {
            UUID id = UUID.randomUUID();
            TransactionResponse resp = new TransactionResponse();
            resp.setId(id);
            resp.setMule(false);
            resp.setCreatedAt(LocalDateTime.now());
            when(svc.process(any(TransactionInput.class))).thenReturn(resp);

            TransactionInput in = new TransactionInput();
            in.setCustomerId("C123");
            in.setDirection(TransactionDirection.INBOUND);
            in.setAmount(BigDecimal.valueOf(6000));
            in.setChannel("ATM");
            in.setTime("02:00");
            in.setCountry("Mordor");
            in.setAccountAgeDays(10);
            in.setActivitySummary("first txn");

            mockMvc.perform(post("/transactions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(in)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.mule").value(false));
        }
    }

    @Nested @DisplayName("GET /transactions/flagged")
    class Flagged {
        @Test @DisplayName("empty list → 204")
        void emptyFlaggedGivesNoContent() throws Exception {
            when(svc.listMules(null)).thenReturn(List.of());
            mockMvc.perform(get("/transactions/flagged"))
                    .andExpect(status().isNoContent());
        }

        @Test @DisplayName("non-empty → 200 + JSON array")
        void nonEmptyFlaggedGivesOk() throws Exception {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            TransactionResponse a = new TransactionResponse(); a.setId(id1);
            TransactionResponse b = new TransactionResponse(); b.setId(id2);
            when(svc.listMules(null)).thenReturn(List.of(a,b));
            mockMvc.perform(get("/transactions/flagged"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[1].id").exists());
        }
    }
}
