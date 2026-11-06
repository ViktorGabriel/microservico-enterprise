package com.enterprise.financial.infrastructure.adapter.in.web;

import com.enterprise.financial.application.service.StatementService;
import com.enterprise.financial.domain.model.Statement;
import com.enterprise.financial.infrastructure.adapter.in.web.dto.StatementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Extratos Bancários", description = "Endpoints de consulta e emissão de extratos consolidados")
public class StatementController {

    private final StatementService statementService;

    public StatementController(StatementService statementService) {
        this.statementService = statementService;
    }

    @GetMapping("/{id}/statement")
    @Operation(summary = "Consultar extrato consolidado", description = "Retorna o extrato consolidado com total de créditos, débitos e lista detalhada de transações no período")
    public ResponseEntity<StatementResponse> getStatement(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate) {
        Statement statement = statementService.generateStatement(id, startDate, endDate);
        return ResponseEntity.ok(StatementResponse.fromDomain(statement));
    }
}
