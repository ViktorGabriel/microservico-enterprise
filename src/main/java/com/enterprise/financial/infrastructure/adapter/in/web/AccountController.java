package com.enterprise.financial.infrastructure.adapter.in.web;

import com.enterprise.financial.application.service.AccountService;
import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.AccountResponse;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.CreateAccountRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Contas Bancárias", description = "Endpoints de gerenciamento e consulta de contas")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(summary = "Criar conta bancária", description = "Cria uma nova conta bancária ativa com verificação de unicidade de número e documento")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        Account created = accountService.createAccount(
                request.accountNumber(),
                request.documentNumber(),
                request.holderName(),
                request.initialDeposit()
        );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(AccountResponse.fromDomain(created));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID", description = "Retorna os detalhes e saldo da conta pelo seu identificador UUID")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Buscar conta por número", description = "Retorna os dados da conta buscando pelo número da conta bancária")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }
}
