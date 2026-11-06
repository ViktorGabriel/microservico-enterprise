# Enterprise Financial Core API

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.3.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![JWT](https://img.shields.io/badge/JWT-Stateless_Auth-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)](https://jwt.io/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger_UI-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)](https://swagger.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Flyway](https://img.shields.io/badge/Flyway-10-CC0202?style=for-the-badge&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Testcontainers](https://img.shields.io/badge/Testcontainers-PostgreSQL-FFA500?style=for-the-badge&logo=testcontainers&logoColor=white)](https://testcontainers.com/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

Motor transacional de alta confiabilidade desenvolvido para processamento bancário e de pagamentos em larga escala. A API implementa princípios de **Clean Architecture (Ports and Adapters)**, garantindo isolamento total do domínio, garantias estritas de idempotência via cabeçalho HTTP (`X-Idempotency-Key`), autenticação stateless **Bearer JWT** e prevenção de concorrência com **Pessimistic Locking** e ordenação determinística de locks para evitar deadlocks.

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
    └── config/                  # Segurança JWT (Spring Security Stateless, Filter) e OpenAPI Swagger
```

### 🛡️ Destaques de Engenharia
1. **Autenticação Stateless com JWT:** Rotas transacionais protegidas via Bearer JWT token assinado via HMAC-SHA256, com tempo de expiração e extração de authorities.
2. **Prevenção Estrita de Deadlocks:** Na transferência entre contas, a aquisição de travas pessimistas no banco de dados é feita de maneira **ordenada deterministicamente pelo UUID** das contas envolvidas (`id1.compareTo(id2) < 0`), eliminando deadlocks causados por concorrência mútua reversa.
3. **Garantia de Idempotência:** Cada operação financeira exige o cabeçalho `X-Idempotency-Key`. Se uma requisição com a mesma chave for reenviada (ex.: retentativas de rede de gateways de pagamento), a operação não é duplicada e o registro original é retornado de forma consistente.
4. **Documentação Viva com Swagger / OpenAPI:** Documentação interativa personalizada em `/swagger-ui.html` com suporte nativo a Bearer Token e injeção do header `X-Idempotency-Key`.
5. **Testcontainers com PostgreSQL 16:** Testes de integração em banco real via Docker container e migrações Flyway.
6. **Precisão Monetária:** Todos os valores são tratados com `BigDecimal` normalizados para 4 casas decimais e arredondamento bancário (`HALF_EVEN`).

---

## 📖 Documentação Interativa (Swagger UI)

Com a aplicação em execução, acesse a interface interativa do Swagger para testar todos os endpoints:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> Para autenticar no Swagger UI, clique no botão **Authorize** no topo da página e informe o token obtido no endpoint `/api/v1/auth/login`.

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

Caso prefira rodar diretamente no host:

```bash
# Copie o arquivo de variáveis de ambiente
cp .env.example .env

# Execute a suíte completa de testes unitários e de integração
./mvnw clean verify

# Inicie a aplicação
./mvnw spring-boot:run
```

---

## 📡 Endpoints da API

### 1. Autenticação (Gerar Token JWT)
- **POST** `/api/v1/auth/login`
- **Request Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```
- **Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

---

### 2. Criar Conta Bancária
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

### 3. Consultar Saldo / Dados da Conta
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

### 4. Realizar Depósito (Crédito) 🔒
- **POST** `/api/v1/transactions/deposit`
- **Headers Obrigatórios:**
  - `Authorization: Bearer <seu_token_jwt>`
  - `X-Idempotency-Key: dep-uuid-12345`
- **Request Body:**
```json
{
  "targetAccountId": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "amount": 250.00,
  "description": "Depósito em conta corrente"
}
```

---

### 5. Realizar Saque (Débito) 🔒
- **POST** `/api/v1/transactions/withdraw`
- **Headers Obrigatórios:**
  - `Authorization: Bearer <seu_token_jwt>`
  - `X-Idempotency-Key: wd-uuid-67890`
- **Request Body:**
```json
{
  "sourceAccountId": "e98e4d3a-18b3-4f9a-bbcb-e67c824c8b21",
  "amount": 100.00,
  "description": "Saque em terminal 24h"
}
```

---

### 6. Realizar Transferência entre Contas 🔒
- **POST** `/api/v1/transactions/transfer`
- **Headers Obrigatórios:**
  - `Authorization: Bearer <seu_token_jwt>`
  - `X-Idempotency-Key: tr-uuid-99999`
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

### 7. Consultar Extrato Consolidado
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

### 8. Monitoramento e Saúde
- **GET** `/actuator/health` -> Retorna `{"status": "UP"}`

---

## 🧪 Suíte de Testes Automatizados

O projeto conta com pirâmide de testes completa com **JUnit 5**, **Mockito**, **Spring Boot Test** e **Testcontainers**:

```powershell
./mvnw clean verify
```

- **Testes Unitários de Domínio:** Invariantes da entidade `Account` (crédito, débito, saldo insuficiente, bloqueio e escala decimal).
- **Testes de Serviço:** Validação de comportamento idempotente, regras de negócio e prevenção de deadlock.
- **Testes de Integração com JWT:** Validação do fluxo de autenticação, rejeição de requisições desprotegidas (401 Unauthorized), geração de tokens e ciclo transacional completo.
- **Testcontainers (PostgreSQL 16):** Validação de persistência real e migrações Flyway em banco de dados conteinerizado.

---

## 📄 Licença
Distribuído sob a licença MIT. Consulte `LICENSE` para mais informações.
