package com.enterprise.financial.infrastructure.adapter.in.web;

import com.enterprise.financial.application.service.TransactionService;
import com.enterprise.financial.domain.model.Transaction;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.DepositRequest;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.TransferRequest;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.WithdrawalRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
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
