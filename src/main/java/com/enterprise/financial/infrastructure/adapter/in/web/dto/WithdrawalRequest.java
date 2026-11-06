package com.enterprise.financial.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record WithdrawalRequest(
        @NotNull(message = "ID da conta de origem é obrigatório")
        UUID sourceAccountId,

        @NotNull(message = "Valor do saque é obrigatório")
        @DecimalMin(value = "0.01", inclusive = true, message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description
) {}
