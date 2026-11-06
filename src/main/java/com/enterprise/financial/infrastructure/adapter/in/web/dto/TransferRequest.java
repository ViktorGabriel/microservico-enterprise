package com.enterprise.financial.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull(message = "ID da conta de origem é obrigatório")
        UUID sourceAccountId,

        @NotNull(message = "ID da conta de destino é obrigatório")
        UUID targetAccountId,

        @NotNull(message = "Valor da transferência é obrigatório")
        @DecimalMin(value = "0.01", inclusive = true, message = "Valor deve ser maior que zero")
        BigDecimal amount,

        @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
        String description
) {}
