package org.example.banksimulator.controller;


import org.example.banksimulator.dto.DepositDto;
import org.example.banksimulator.enums.TransactionStatus;
import org.example.banksimulator.enums.TransactionType;
import org.example.banksimulator.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void deposit_shouldReturnOk() throws Exception {
        DepositDto dto = new DepositDto(TransactionType.DEPOSIT,
                new BigDecimal("10000.1"),
                TransactionStatus.COMPLETED,
                1L,
                "KZ121212121212331");
        when(transactionService.deposit(1L, new BigDecimal("10000.1"))).thenReturn(dto);

        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "accountId": 1,
                                   "depositAmount": 10000.1                    
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(10000.1))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
        verify(transactionService).deposit(1L, new BigDecimal("10000.1"));
    }

    @Test
    void deposit_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/transactions/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "accountId": 1,
                                "depositAmount": -10000.1                            }
                            """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }


}
