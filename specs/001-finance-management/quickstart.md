# Quickstart & Validation Guide: Sistema de Gestão Financeira

**Feature**: 001-finance-management | **Date**: 2026-10-04 | **Phase**: 1

Este guia descreve como validar que o sistema funciona end-to-end após a implementação.  
Para detalhes de contrato de API, veja [`contracts/api-contract.md`](./contracts/api-contract.md).  
Para o modelo de dados, veja [`data-model.md`](./data-model.md).

---

## Pré-requisitos

- Java 17+ instalado
- Node.js 20+ instalado
- MySQL 8.x rodando localmente (ou via Docker: `docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=financia mysql:8`)
- Backend configurado (`application.yml` com datasource apontando para o MySQL local)
- Frontend servido (`ng serve` ou build servido em um servidor estático)
- Ferramenta HTTP disponível (curl, Postman, Insomnia ou o próprio navegador)

---

## Cenário 1 — Registro e saldo básico (valida US1, US2, US3)

**Objetivo**: confirmar que entradas e saídas afetam o saldo corretamente e aparecem no histórico.

### Passo 1: Registrar usuário e fazer login

```http
POST /api/auth/register
{ "name": "Teste", "email": "teste@financia.com", "password": "Senha@123" }
→ Esperado: 201 com id do usuário

POST /api/auth/login
{ "email": "teste@financia.com", "password": "Senha@123" }
→ Esperado: 200 com { "token": "..." }
→ Salvar token para as próximas requisições
```

### Passo 2: Registrar uma entrada

```http
POST /api/income
Authorization: Bearer {token}
{ "date": "2026-10-01", "amount": 3000.00, "description": "Salário outubro" }
→ Esperado: 201 com o registro criado
```

### Passo 3: Registrar uma saída confirmada

```http
POST /api/expense
Authorization: Bearer {token}
{ "date": "2026-10-02", "amount": 500.00, "description": "Supermercado" }
→ Esperado: 201 com payment_status = "CONFIRMED"
```

### Passo 4: Verificar saldo

```http
GET /api/balance
→ Esperado: { "balance": 2500.00 }
```

### Passo 5: Verificar histórico

```http
GET /api/history?from=2026-10-01&to=2026-10-31
→ Esperado: 2 rows
  - 2026-10-01: income = 3000.00, running_balance = 3000.00
  - 2026-10-02: expense = 500.00, running_balance = 2500.00
```

✅ **Critério de aceite**: saldo = 2500.00, histórico com 2 linhas em ordem cronológica com saldo acumulado correto.

---

## Cenário 2 — Lançamento retroativo recalcula saldo (valida FR-011)

**Objetivo**: confirmar que inserir uma entrada com data passada recalcula o saldo de todos os dias posteriores.

Continuando do Cenário 1 (saldo atual = 2500.00):

```http
POST /api/income
Authorization: Bearer {token}
{ "date": "2026-09-30", "amount": 200.00, "description": "Freelance retroativo" }
→ Esperado: 201

GET /api/history?from=2026-09-30&to=2026-10-31
→ Esperado: 3 rows
  - 2026-09-30: income = 200.00,  running_balance =  200.00
  - 2026-10-01: income = 3000.00, running_balance = 3200.00
  - 2026-10-02: expense = 500.00, running_balance = 2700.00

GET /api/balance
→ Esperado: { "balance": 2700.00 }
```

✅ **Critério de aceite**: saldo atualizado para 2700.00; histórico de 01/10 e 02/10 com saldos recalculados.

---

## Cenário 3 — Cartão de crédito pendente e fatura (valida FR-007, US3)

**Objetivo**: confirmar que gastos de cartão aparecem como pendentes e são debitados no vencimento.

### Passo 1: Criar cartão de crédito

```http
POST /api/payment-methods
Authorization: Bearer {token}
{ "name": "Nubank", "type": "CREDIT_CARD", "due_day": 10, "billing_start_day": 11, "billing_end_day": 10 }
→ Esperado: 201
```

### Passo 2: Registrar despesa no cartão

```http
POST /api/expense
Authorization: Bearer {token}
{ "date": "2026-10-05", "amount": 300.00, "payment_method_id": {id_nubank}, "description": "Compra online" }
→ Esperado: 201 com payment_status = "PENDING_CREDIT"
```

### Passo 3: Verificar histórico — pendente não afeta saldo

```http
GET /api/history?from=2026-10-01&to=2026-10-09
→ Row de 2026-10-05 deve conter:
  - entry com status = "PENDING_CREDIT", amount = 300.00
  - running_balance NÃO descontado (mantém saldo anterior ao dia 05)
```

### Passo 4: Verificar fatura no dia de vencimento

```http
GET /api/history?from=2026-10-01&to=2026-10-31
→ Row de 2026-10-10 deve conter:
  - entry do tipo "CREDIT_CARD_BILL", label = "Fatura Nubank", amount = 300.00
  - running_balance descontado em 300.00 a partir de 2026-10-10
```

✅ **Critério de aceite**: gasto pendente não afeta saldo antes do dia 10; fatura aparece no dia 10 e desconta o saldo.

---

## Cenário 4 — Transação recorrente mensal (valida FR-009, US4)

**Objetivo**: confirmar que uma saída mensal recorrente é projetada nos meses futuros.

```http
POST /api/expense
Authorization: Bearer {token}
{
  "date": "2026-10-05",
  "amount": 1200.00,
  "description": "Aluguel",
  "recurrence": { "frequency": "MONTHLY", "start_date": "2026-10-05" }
}
→ Esperado: 201

GET /api/history?from=2026-10-01&to=2026-12-31
→ Esperado: rows em 2026-10-05, 2026-11-05 e 2026-12-05 com amount = 1200.00
→ Linhas de novembro e dezembro com is_recurring_projection = true
```

✅ **Critério de aceite**: projeção de aluguel aparece nos 3 meses sem registros adicionais no banco.

---

## Cenário 5 — Editar ocorrência recorrente (valida FR-009a)

**Objetivo**: confirmar o diálogo de 3 opções e o comportamento de exceção.

```http
PUT /api/expense/{id_aluguel}
Authorization: Bearer {token}
{ "amount": 1300.00, "recurrence_scope": "THIS_ONLY" }
→ Esperado: 200

GET /api/history?from=2026-10-01&to=2026-12-31
→ 2026-10-05: amount = 1300.00 (modificado)
→ 2026-11-05: amount = 1200.00 (inalterado)
→ 2026-12-05: amount = 1200.00 (inalterado)
```

✅ **Critério de aceite**: somente a ocorrência de outubro foi alterada.

---

## Cenário 6 — Relatório mensal com exportação (valida FR-016 a FR-020, US5)

**Objetivo**: confirmar painel de relatório e exportação CSV.

```http
GET /api/reports?filter=MONTH&year=2026&month=10
→ Esperado: JSON com totals, category_breakdown e projections preenchidos
→ totals.income deve somar todas as entradas de outubro
→ category_breakdown deve listar as categorias usadas com percentuais somando 100%

GET /api/reports/export?filter=MONTH&year=2026&month=10
→ Esperado: download de arquivo .csv com as transações do mês
→ Content-Type: text/csv
```

✅ **Critério de aceite**: totais do relatório batem com os valores do histórico do mesmo período; CSV é baixado com sucesso.

---

## Cenário 7 — Configurações e dark mode (valida FR-021)

```http
PUT /api/users/me
Authorization: Bearer {token}
{ "theme_preference": "DARK" }
→ Esperado: 200 com theme_preference = "DARK"

GET /api/users/me
→ Esperado: theme_preference = "DARK" persistido
```

✅ **Critério de aceite**: preferência de tema salva e recuperada corretamente.

---

## Cenário 8 — Exclusão de categoria com transações (valida FR-022)

```http
DELETE /api/categories/{id_categoria_usada}
→ Esperado: 204 (exclusão lógica — active = false)

GET /api/categories
→ A categoria NÃO deve aparecer na lista

GET /api/history?from=2026-10-01&to=2026-10-31
→ Despesas que usavam a categoria devem ainda aparecer com o nome da categoria preservado
   (campo category_name_snapshot)
```

✅ **Critério de aceite**: categoria some das listas, mas histórico preserva o nome.

---

## Matriz de Cobertura

| Cenário | User Story | FRs Validados |
|---------|-----------|---------------|
| 1 — Saldo básico | US1, US2, US3 | FR-001, FR-002, FR-008, FR-010, FR-011 |
| 2 — Retroativo | US1 | FR-011 |
| 3 — Cartão pendente/fatura | US3 | FR-006, FR-007 |
| 4 — Recorrência mensal | US4 | FR-009, FR-013 |
| 5 — Editar ocorrência | US4 | FR-009a |
| 6 — Relatório + CSV | US5 | FR-016, FR-017, FR-018, FR-019, FR-020 |
| 7 — Dark mode | — | FR-021 |
| 8 — Exclusão órfã | — | FR-022 |
