package com.payhub.bankcore.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CoreTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateTransactionWithAuditDimensions() throws Exception {
        mockMvc.perform(post("/core/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "REQ-1001",
                                  "bizOrderId": "BIZ-1001",
                                  "bizType": "PAYMENT",
                                  "txnType": "PAY_IN",
                                  "customerNo": "CUST-1001",
                                  "amount": 88.50,
                                  "currency": "CNY",
                                  "debitAccountNo": "ACC-DR-1001",
                                  "debitAccountSeqNo": 10001,
                                  "debitSubjectCode": "100201",
                                  "creditAccountNo": "ACC-CR-2001",
                                  "creditAccountSeqNo": 20001,
                                  "creditSubjectCode": "200101",
                                  "occurredAt": "2026-04-04T13:00:00",
                                  "remark": "bootstrap request"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("REQ-1001"))
                .andExpect(jsonPath("$.customerNo").value("CUST-1001"))
                .andExpect(jsonPath("$.debitAccountSeqNo").value(10001))
                .andExpect(jsonPath("$.creditSubjectCode").value("200101"))
                .andExpect(jsonPath("$.status").value("INIT"));
    }

    @Test
    void shouldQueryByRequestId() throws Exception {
        mockMvc.perform(post("/core/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "REQ-2001",
                                  "bizOrderId": "BIZ-2001",
                                  "bizType": "PAYMENT",
                                  "txnType": "PAY_IN",
                                  "customerNo": "CUST-2001",
                                  "amount": 66.00,
                                  "currency": "CNY",
                                  "debitAccountNo": "ACC-DR-2001",
                                  "debitAccountSeqNo": 30001,
                                  "debitSubjectCode": "100301",
                                  "creditAccountNo": "ACC-CR-2001",
                                  "creditAccountSeqNo": 30002,
                                  "creditSubjectCode": "200301",
                                  "occurredAt": "2026-04-04T13:05:00"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/core/transactions").param("requestId", "REQ-2001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bizOrderId").value("BIZ-2001"))
                .andExpect(jsonPath("$.rawCode").value("FOUND"));
    }
}
