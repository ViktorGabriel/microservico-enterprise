package com.enterprise.financial.infrastructure.adapter.in.web;

import com.enterprise.financial.application.service.AccountService;
import com.enterprise.financial.domain.model.Account;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.AccountResponse;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.CreateAccountRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
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
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id) {
        Account account = accountService.getAccountById(id);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(AccountResponse.fromDomain(account));
    }
}
