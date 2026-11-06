# Enterprise Financial Core API

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-10-CC0202?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

Motor transacional de alta confiabilidade desenvolvido para processamento bancário e de pagamentos em larga escala. A API implementa princípios de **Clean Architecture (Ports and Adapters)**, garantindo isolamento total do domínio, garantias estritas de idempotência via cabeçalho HTTP (`X-Idempotency-Key`) e prevenção de concorrência com **Pessimistic Locking** e ordenação determinística de locks para evitar deadlocks.

---

## 🏛️ Arquitetura e Design Patterns

A aplicação segue a separação estrita da Arquitetura Hexagonal:

```
src/main/java/com/enterprise/financial
├── domain/                      # Camada de Domínio (Pure Java, Agnóstica de Framework)
│   ├── model/                   # Entidades e Value Objects (Account, Transaction, Statement)
│   ├── exception/               # Exceções de negócio (InsufficientBalance, AccountBlocked, etc.)
│   └── port/                    # Portas de Repositório (AccountRepositoryPort, TransactionRepositoryPort)
├── application/                 # Camada de Aplicação / Casos de Uso
│   └── service/                 # Serviços orquestradores (AccountService, TransactionService, StatementService)
└── infrastructure/              # Adaptadores de Entrada e Saída
    ├── adapter/
    │   ├── in/web/              # Adaptador REST (Controllers, Records DTOs, ExceptionHandler)
    │   └── out/persistence/     # Adaptador JPA (Entities, Spring Data Repositories, Mappers)
    └── config/                  # Segurança (Spring Security Stateless) e Configurações
```

### 🛡️ Destaques de Engenharia
1. **Prevenção Estrita de Deadlocks:** Na transferência entre contas, a aquisição de travas pessimistas no banco de dados é feita de maneira **ordenada deterministicamente pelo UUID** das contas envolvidas (`id1.compareTo(id2) < 0`), eliminando deadlocks causados por concorrência mútua reversa.
2. **Garantia de Idempotência:** Cada operação financeira exige o cabeçalho `X-Idempotency-Key`. Se uma requisição com a mesma chave for reenviada (ex.: retentativas de rede de gateways de pagamento), a operação não é duplicada e o registro original é retornado de forma consistente.
3. **Precisão Monetária:** Todos os valores são tratados com `BigDecimal` normalizados para 4 casas decimais e arredondamento bancário (`HALF_EVEN`).
4. **Zero-Leak & Security:** Migrations automatizadas com Flyway, sanitização em `.gitignore` e Spring Security stateless.

---

## 🚀 Como Executar

### Pré-requisitos
- **Docker** e **Docker Compose**
- *(Opcional para execução nativa)* **Java 21 JDK** e **Maven 3.9+**

### 1. Inicialização via Docker Compose (Recomendado)

Clone o repositório e inicie os containers da API e do PostgreSQL:

```bash
# Iniciar o banco PostgreSQL e a API containerizada
docker compose up -d --build

# Acompanhar os logs da aplicação
docker compose logs -f api
```

A API estará disponível em `http://localhost:8080`.

### 2. Execução Local via Maven Wrapper

Caso prefira rodar diretamente no host com um banco PostgreSQL local ou containerizado:

```bash
# Copie o arquivo de variáveis de ambiente
cp .env.example .env

# Execute a suíte de testes unitários e de integração
./mvnw clean test

# Inicie a aplicação
./mvnw spring-boot:run
```

---

## 📡 Endpoints da API

### 1. Criar Conta Bancária
- **POST** `/api/v1/accounts`
- **Request Body:**
```json
{
  "accountNumber": "00010203",
  "documentNumber": "12345678901",
  "holderName": "Viktor Gabriel",
  "initialDeposit": 1500.00
}
```
- **Response (201 Created):**
```json
{
  "id": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "accountNumber": "00010203",
  "documentNumber": "12345678901",
  "holderName": "Viktor Gabriel",
  "balance": 1500.0000,
  "status": "ACTIVE",
  "createdAt": "2026-11-06T18:00:00Z",
  "updatedAt": "2026-11-06T18:00:00Z"
}
```

---

### 2. Consultar Saldo / Dados da Conta
- **GET** `/api/v1/accounts/{id}`
- **Response (200 OK):**
```json
{
  "id": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "accountNumber": "00010203",
  "documentNumber": "12345678901",
  "holderName": "Viktor Gabriel",
  "balance": 1500.0000,
  "status": "ACTIVE"
}
```

---

### 3. Realizar Depósito (Crédito)
- **POST** `/api/v1/transactions/deposit`
- **Header Obrigatório:** `X-Idempotency-Key: dep-uuid-12345`
- **Request Body:**
```json
{
  "targetAccountId": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "amount": 250.00,
  "description": "Depósito em conta corrente"
}
```

---

### 4. Realizar Saque (Débito)
- **POST** `/api/v1/transactions/withdraw`
- **Header Obrigatório:** `X-Idempotency-Key: wd-uuid-67890`
- **Request Body:**
```json
{
  "sourceAccountId": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "amount": 100.00,
  "description": "Saque em terminal 24h"
}
```

---

### 5. Realizar Transferência entre Contas
- **POST** `/api/v1/transactions/transfer`
- **Header Obrigatório:** `X-Idempotency-Key: tr-uuid-99999`
- **Request Body:**
```json
{
  "sourceAccountId": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "targetAccountId": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "amount": 350.00,
  "description": "Pagamento de serviços"
}
```

---

### 6. Consultar Extrato Consolidado
- **GET** `/api/v1/accounts/{id}/statement?startDate=2026-11-01T00:00:00Z&endDate=2026-11-30T23:59:59Z`
- **Response (200 OK):**
```json
{
  "accountId": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "accountNumber": "00010203",
  "holderName": "Viktor Gabriel",
  "currentBalance": 1300.0000,
  "periodStart": "2026-11-01T00:00:00Z",
  "periodEnd": "2026-11-30T23:59:59Z",
  "totalCredits": 250.0000,
  "totalDebits": 450.0000,
  "items": [
    {
      "transactionId": "...",
      "idempotencyKey": "tr-uuid-99999",
      "type": "TRANSFER",
      "amount": 350.0000,
      "entryType": "DEBIT",
      "description": "Pagamento de serviços",
      "timestamp": "2026-11-06T18:15:00Z"
    }
  ]
}
```

---

### 7. Monitoramento e Saúde
- **GET** `/actuator/health` -> Retorna `{"status": "UP"}`

---

## 🧪 Suíte de Testes Automatizados

O projeto conta com pirâmide de testes completa com **JUnit 5**, **Mockito** e **Spring Boot Test**:

```powershell
./mvnw test
```

- **Testes Unitários de Domínio:** Invariantes da entidade `Account` (crédito, débito, saldo insuficiente, bloqueio e escala decimal).
- **Testes de Serviço:** Validação de comportamento idempotente, regras de negócio e prevenção de deadlock.
- **Testes de Integração End-to-End:** Ciclo de vida completo das transações e validações de cabeçalhos e respostas HTTP.

---

## 📄 Licença
Distribuído sob a licença MIT. Consulte `LICENSE` para mais informações.
