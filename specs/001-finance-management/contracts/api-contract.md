# API Contract: Sistema de Gestão Financeira

**Feature**: 001-finance-management | **Date**: 2026-10-04 | **Phase**: 1  
**Base URL**: `http://localhost:8080/api`  
**Auth**: Bearer JWT em todos os endpoints (exceto `/auth/*`)

---

## Convenções

- Datas: formato `YYYY-MM-DD`
- Valores monetários: `number` com 2 casas decimais (BRL)
- Erros: `{ "error": "mensagem", "field": "campo" }` (field opcional)
- Paginação: não aplicável em v1 (volumes pessoais pequenos)

---

## Autenticação

### `POST /auth/register`
Registra novo usuário.

**Request**
```json
{ "name": "João Silva", "email": "joao@email.com", "password": "minhaSenha123" }
```
**Response 201**
```json
{ "id": 1, "name": "João Silva", "email": "joao@email.com" }
```
**Errors**: 409 email já cadastrado | 422 campos inválidos

---

### `POST /auth/login`
Autentica e retorna JWT.

**Request**
```json
{ "email": "joao@email.com", "password": "minhaSenha123" }
```
**Response 200**
```json
{ "token": "eyJhbGci...", "expires_at": "2026-10-05T21:00:00Z" }
```
**Errors**: 401 credenciais inválidas

---

## Perfil e Configurações

### `GET /users/me`
Retorna dados do usuário autenticado.

**Response 200**
```json
{ "id": 1, "name": "João Silva", "email": "joao@email.com", "theme_preference": "LIGHT" }
```

---

### `PUT /users/me`
Atualiza nome, email ou preferência de tema.

**Request**
```json
{ "name": "João Silva", "email": "joao@email.com", "theme_preference": "DARK" }
```
**Response 200**: mesmo formato de `GET /users/me`  
**Errors**: 409 email já em uso | 422 campos inválidos

---

### `PUT /users/me/password`
Altera senha do usuário.

**Request**
```json
{ "current_password": "minhaSenha123", "new_password": "novaSenha456" }
```
**Response 204** (sem body)  
**Errors**: 401 senha atual incorreta

---

### `DELETE /users/me`
Exclui conta e todos os dados do usuário.

**Response 204** (sem body)

---

## Saldo

### `GET /balance`
Retorna o saldo total atual do usuário.

**Response 200**
```json
{ "balance": 1842.50, "calculated_at": "2026-10-04" }
```

> Saldo = soma de todas as entradas − soma de todas as saídas `CONFIRMED` até hoje.

---

## Histórico

### `GET /history`
Retorna o histórico em formato de planilha por data.

**Query params**: `from=YYYY-MM-DD&to=YYYY-MM-DD` (obrigatório)

**Response 200**
```json
{
  "from": "2026-09-01",
  "to": "2026-09-30",
  "rows": [
    {
      "date": "2026-09-05",
      "entries": [
        {
          "id": 10,
          "type": "INCOME",
          "amount": 5000.00,
          "label": "Salário — Empresa X",
          "status": "CONFIRMED",
          "is_recurring_projection": false
        },
        {
          "id": 22,
          "type": "EXPENSE",
          "amount": 150.00,
          "label": "Jantar — Lazer",
          "status": "PENDING_CREDIT",
          "payment_method": "Nubank",
          "is_recurring_projection": false
        }
      ],
      "daily_income": 5000.00,
      "daily_expense_confirmed": 0.00,
      "daily_expense_pending": 150.00,
      "running_balance": 3342.50
    },
    {
      "date": "2026-09-10",
      "entries": [
        {
          "id": null,
          "type": "CREDIT_CARD_BILL",
          "amount": 800.00,
          "label": "Fatura Nubank",
          "status": "CONFIRMED",
          "is_recurring_projection": false
        }
      ],
      "daily_income": 0.00,
      "daily_expense_confirmed": 800.00,
      "daily_expense_pending": 0.00,
      "running_balance": 2542.50
    }
  ]
}
```

> - `running_balance`: saldo acumulado até e incluindo essa data, considerando apenas `CONFIRMED`.  
> - `is_recurring_projection: true`: ocorrência futura gerada a partir de regra de recorrência.  
> - `id: null`: linha sintética de fatura de cartão (não é um registro persistido).  
> - Dias sem lançamentos **aparecem** no array `rows` (uma linha por dia civil do intervalo). Ver `specs/002-history-every-day/contracts/api-contract.md`.

---

## Entradas (Income)

### `POST /income`
Registra nova entrada.

**Request**
```json
{
  "date": "2026-09-05",
  "amount": 5000.00,
  "income_source_id": 3,
  "description": "Salário setembro",
  "recurrence": {
    "frequency": "MONTHLY",
    "start_date": "2026-09-05",
    "end_date": null
  }
}
```
> `income_source_id`, `description` e `recurrence` são opcionais.

**Response 201**
```json
{ "id": 10, "date": "2026-09-05", "amount": 5000.00, "source": "Empresa X", "description": "Salário setembro" }
```
**Errors**: 422 date ou amount ausentes | 404 income_source_id não encontrado

---

### `PUT /income/{id}`
Edita uma entrada. Para recorrentes, inclui `recurrence_scope`.

**Request**
```json
{
  "date": "2026-09-05",
  "amount": 5200.00,
  "description": "Salário setembro com bônus",
  "recurrence_scope": "THIS_ONLY"
}
```
> `recurrence_scope`: `THIS_ONLY` | `THIS_AND_FUTURE` | `ALL` (obrigatório se lançamento é recorrente)

**Response 200**: mesma estrutura do POST

---

### `DELETE /income/{id}`
Remove uma entrada. Para recorrentes, inclui `recurrence_scope`.

**Request body**
```json
{ "recurrence_scope": "THIS_AND_FUTURE" }
```
**Response 204**

---

## Saídas (Expense)

### `POST /expense`
Registra nova saída.

**Request**
```json
{
  "date": "2026-09-15",
  "amount": 150.00,
  "expense_category_id": 5,
  "payment_method_id": 2,
  "description": "Jantar com amigos",
  "recurrence": null
}
```
> `expense_category_id`, `payment_method_id`, `description` e `recurrence` são opcionais.  
> Se `payment_method` for do tipo `CREDIT_CARD`, o backend define `payment_status = PENDING_CREDIT` automaticamente.

**Response 201**
```json
{
  "id": 22,
  "date": "2026-09-15",
  "amount": 150.00,
  "category": "Lazer",
  "payment_method": "Nubank",
  "payment_status": "PENDING_CREDIT",
  "description": "Jantar com amigos"
}
```
**Errors**: 422 date ou amount ausentes

---

### `PUT /expense/{id}`
Edita uma saída (mesmo formato do income, com `recurrence_scope` se aplicável).

### `DELETE /expense/{id}`
Remove uma saída (mesmo formato do income).

---

## Cadastros Auxiliares

### Fontes de Receita

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/income-sources` | Lista todas as fontes ativas do usuário |
| `POST` | `/income-sources` | Cria nova fonte `{ "name": "Vendas de bolo" }` |
| `PUT` | `/income-sources/{id}` | Renomeia `{ "name": "Novo nome" }` |
| `DELETE` | `/income-sources/{id}` | Marca como inativa (orphan — FR-022) |

**GET Response**
```json
[{ "id": 3, "name": "Empresa X" }, { "id": 4, "name": "Vendas de bolo" }]
```

---

### Categorias de Gasto

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/categories` | Lista todas as categorias ativas |
| `POST` | `/categories` | `{ "name": "Lazer" }` |
| `PUT` | `/categories/{id}` | `{ "name": "Novo nome" }` |
| `DELETE` | `/categories/{id}` | Marca como inativa (orphan) |

---

### Fontes de Pagamento

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| `GET` | `/payment-methods` | Lista todas as fontes ativas |
| `POST` | `/payment-methods` | Cria nova fonte |
| `PUT` | `/payment-methods/{id}` | Atualiza |
| `DELETE` | `/payment-methods/{id}` | Marca como inativa (orphan) |

**POST Request — Conta corrente/reserva**
```json
{ "name": "Conta Bradesco", "type": "CHECKING" }
```

**POST Request — Cartão de crédito**
```json
{
  "name": "Nubank",
  "type": "CREDIT_CARD",
  "due_day": 10,
  "billing_start_day": 11,
  "billing_end_day": 10
}
```
> `due_day`, `billing_start_day`, `billing_end_day` obrigatórios quando `type = CREDIT_CARD` (FR-006).

---

## Relatórios

### `GET /reports`
Gera relatório consolidado para o período.

**Query params** (um dos três obrigatório):
- Intervalo: `from=YYYY-MM-DD&to=YYYY-MM-DD`
- Mês fechado: `filter=MONTH&year=2026&month=9`
- Ano fechado: `filter=YEAR&year=2026`

**Response 200**
```json
{
  "period": { "from": "2026-09-01", "to": "2026-09-30" },
  "totals": {
    "income": 5000.00,
    "expenses_confirmed": 2400.00,
    "expenses_credit_card": 800.00,
    "savings": 1800.00
  },
  "category_breakdown": [
    { "category": "Lazer", "amount": 400.00, "percentage": 12.5 },
    { "category": "Contas Fixas", "amount": 2000.00, "percentage": 62.5 },
    { "category": "Sem categoria", "amount": 800.00, "percentage": 25.0 }
  ],
  "projections": {
    "income": [
      { "date": "2026-09-05", "cumulative": 5000.00 }
    ],
    "expenses": [
      { "date": "2026-09-10", "cumulative": 800.00 },
      { "date": "2026-09-15", "cumulative": 1200.00 }
    ],
    "savings": [
      { "date": "2026-09-05", "cumulative": 5000.00 },
      { "date": "2026-09-10", "cumulative": 4200.00 },
      { "date": "2026-09-15", "cumulative": 3800.00 }
    ]
  }
}
```

### `GET /reports/export`
Retorna CSV com os dados do relatório. Aceita os mesmos query params de `GET /reports`.

**Response 200**
```
Content-Type: text/csv
Content-Disposition: attachment; filename="relatorio-2026-09.csv"

Data,Tipo,Descricao,Categoria,Fonte,Valor
2026-09-05,Entrada,Salário setembro,—,Empresa X,5000.00
2026-09-10,Saída (Fatura),Fatura Nubank,—,Nubank,-800.00
```

---

## Códigos de Erro Padrão

| Código | Significado |
|--------|-------------|
| 400 | Requisição malformada |
| 401 | Não autenticado (token ausente ou expirado) |
| 403 | Acesso negado (recurso de outro usuário) |
| 404 | Recurso não encontrado |
| 409 | Conflito (ex: email já cadastrado) |
| 422 | Dados inválidos (campos obrigatórios ou regras de negócio) |
| 500 | Erro interno do servidor |
