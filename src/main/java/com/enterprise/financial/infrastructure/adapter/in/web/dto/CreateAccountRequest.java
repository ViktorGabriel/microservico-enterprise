package com.enterprise.financial.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotBlank(message = "Número da conta é obrigatório")
        @Size(min = 4, max = 20, message = "Número da conta deve ter entre 4 e 20 caracteres")
        String accountNumber,

        @NotBlank(message = "Documento (CPF ou CNPJ) é obrigatório")
        @Pattern(regexp = "\\d{11}|\\d{14}", message = "Documento deve conter 11 (CPF) ou 14 (CNPJ) dígitos numéricos")
        String documentNumber,

        @NotBlank(message = "Nome do titular é obrigatório")
        @Size(min = 2, max = 120, message = "Nome do titular deve ter entre 2 e 120 caracteres")
        String holderName,

        @DecimalMin(value = "0.0", inclusive = true, message = "Depósito inicial deve ser maior ou igual a zero")
        BigDecimal initialDeposit
) {}
