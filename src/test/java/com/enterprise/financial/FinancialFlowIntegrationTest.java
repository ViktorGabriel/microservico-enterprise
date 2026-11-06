package com.enterprise.financial;

import com.enterprise.financial.infrastructure.adapter.in.web.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Financial Flow End-to-End Integration Tests")
class FinancialFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should execute complete financial lifecycle: accounts, deposit, transfer, idempotency, statement")
    void shouldExecuteFullFinancialLifecycle() throws Exception {
        // 1. Actuator Health Check
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        // 2. Create Account A (Alice)
        CreateAccountRequest reqA = new CreateAccountRequest(
                "ACC-10001",
                "11122233344",
                "Alice Enterprise",
                new BigDecimal("1000.00")
        );

        MvcResult resA = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC-10001"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andReturn();

        AccountResponse accountA = objectMapper.readValue(resA.getResponse().getContentAsString(), AccountResponse.class);
        assertNotNull(accountA.id());

        // 3. Create Account B (Bob)
        CreateAccountRequest reqB = new CreateAccountRequest(
                "ACC-20002",
                "55566677788",
                "Bob Enterprise",
                new BigDecimal("200.00")
        );

        MvcResult resB = mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("ACC-20002"))
                .andExpect(jsonPath("$.balance").value(200.0))
                .andReturn();

        AccountResponse accountB = objectMapper.readValue(resB.getResponse().getContentAsString(), AccountResponse.class);
        assertNotNull(accountB.id());

        // 4. Deposit into Account A
        DepositRequest depositReq = new DepositRequest(
                accountA.id(),
                new BigDecimal("500.00"),
                "Aporte Financeiro"
        );

        mockMvc.perform(post("/api/v1/transactions/deposit")
                        .header("X-Idempotency-Key", "dep-e2e-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 5. Transfer from Account A to Account B
        TransferRequest transferReq = new TransferRequest(
                accountA.id(),
                accountB.id(),
                new BigDecimal("300.00"),
                "Transferência Fornecedor"
        );

        MvcResult resTransfer = mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("X-Idempotency-Key", "transfer-e2e-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("TRANSFER"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        TransactionResponse tx1 = objectMapper.readValue(resTransfer.getResponse().getContentAsString(), TransactionResponse.class);

        // 6. Test Idempotency: Re-submitting identical transfer with the same idempotency key
        MvcResult resDuplicateTransfer = mockMvc.perform(post("/api/v1/transactions/transfer")
                        .header("X-Idempotency-Key", "transfer-e2e-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isCreated())
                .andReturn();

        TransactionResponse txDuplicate = objectMapper.readValue(resDuplicateTransfer.getResponse().getContentAsString(), TransactionResponse.class);
        assertEquals(tx1.id(), txDuplicate.id());

        // 7. Verify balances
        // Account A: 1000 + 500 - 300 = 1200
        mockMvc.perform(get("/api/v1/accounts/" + accountA.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1200.0));

        // Account B: 200 + 300 = 500
        mockMvc.perform(get("/api/v1/accounts/" + accountB.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.0));

        // 8. Attempt withdrawal with insufficient funds on Account B
        WithdrawalRequest withdrawReq = new WithdrawalRequest(
                accountB.id(),
                new BigDecimal("9999.00"),
                "Saque excessivo"
        );

        mockMvc.perform(post("/api/v1/transactions/withdraw")
                        .header("X-Idempotency-Key", "wd-excessive-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdrawReq)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));

        // 9. Fetch Statement for Account A
        mockMvc.perform(get("/api/v1/accounts/" + accountA.id() + "/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-10001"))
                .andExpect(jsonPath("$.currentBalance").value(1200.0))
                .andExpect(jsonPath("$.items").isArray());
    }
}
