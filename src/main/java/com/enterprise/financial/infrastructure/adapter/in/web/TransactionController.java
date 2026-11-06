package com.enterprise.financial.infrastructure.adapter.in.web;

import com.enterprise.financial.application.service.TransactionService;
import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.DepositRequest;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.TransferRequest;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.WithdrawalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transações Financeiras", description = "Operações financeiras protegidas por Bearer JWT com controle de idempotência")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    @Operation(summary = "Realizar depósito", description = "Credita valor na conta de destino de forma atômica e idempotente.")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestHeader(value = "X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody DepositRequest request) {
        Transaction tx = transactionService.processDeposit(
                idempotencyKey,
                request.targetAccountId(),
                request.amount(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromDomain(tx));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Realizar saque", description = "Debita valor da conta de origem validando saldo disponível sob trava pessimista.")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestHeader(value = "X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody WithdrawalRequest request) {
        Transaction tx = transactionService.processWithdrawal(
                idempotencyKey,
                request.sourceAccountId(),
                request.amount(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromDomain(tx));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Realizar transferência", description = "Transfere valor entre contas com garantia de prevenção de deadlocks (ordenação determinística de locks).")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader(value = "X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        Transaction tx = transactionService.processTransfer(
                idempotencyKey,
                request.sourceAccountId(),
                request.targetAccountId(),
                request.amount(),
                request.description()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(TransactionResponse.fromDomain(tx));
    }
}
