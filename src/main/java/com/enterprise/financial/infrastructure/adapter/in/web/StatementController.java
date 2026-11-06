package com.enterprise.financial.infrastructure.adapter.in.web;

import com.enterprise.financial.application.service.StatementService;
import com.enterprise.financial.domain.model.Statement;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.StatementResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<StatementResponse> getStatement(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        Statement statement = statementService.generateStatement(id, startDate, endDate);
        return ResponseEntity.ok(StatementResponse.fromDomain(statement));
    }
}
