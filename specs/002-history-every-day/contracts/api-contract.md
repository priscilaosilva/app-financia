# API Contract: Histórico contínuo e exclusão do dia

**Feature**: 002-history-every-day | **Base**: estende `GET /history` de 001  
**Prefixo**: `/api`

Autenticação: Bearer JWT (igual 001).

---

## `GET /history`

**Query**: `from`, `to` (obrigatórios) — inalterado.

**Mudança de comportamento**: `rows` contém **exatamente um elemento por dia civil** de `from` a `to` inclusive, mesmo sem lançamentos.

**Response 200** (trecho com dia vazio):

```json
{
  "from": "2026-10-01",
  "to": "2026-10-03",
  "rows": [
    {
      "date": "2026-10-01",
      "entries": [
        {
          "id": 10,
          "type": "INCOME",
          "amount": 3000.00,
          "label": "Salário",
          "status": "CONFIRMED",
          "is_recurring_projection": false
        }
      ],
      "daily_income": 3000.00,
      "daily_expense_confirmed": 0.00,
      "daily_expense_pending": 0.00,
      "running_balance": 3000.00
    },
    {
      "date": "2026-10-02",
      "entries": [],
      "daily_income": 0.00,
      "daily_expense_confirmed": 0.00,
      "daily_expense_pending": 0.00,
      "running_balance": 3000.00
    },
    {
      "date": "2026-10-03",
      "entries": [],
      "daily_income": 0.00,
      "daily_expense_confirmed": 0.00,
      "daily_expense_pending": 0.00,
      "running_balance": 3000.00
    }
  ]
}
```

**Notas**:
- Dia vazio: `entries: []`, totais 0, `running_balance` igual ao dia anterior (ou saldo de abertura no primeiro dia).
- `id: null` + `type: CREDIT_CARD_BILL` continua sendo fatura sintética — **não** entra no lote de exclusão.
- Tamanho de `rows` = número de dias do intervalo.

A nota da 001 (“dias sem lançamentos são omitidos”) fica **revogada** para este endpoint.

`GET /balance` não muda.

---

## `POST /history/day-deletions`

Exclui o subconjunto marcado do diálogo de um dia, em uma transação.

**Request**

```json
{
  "date": "2026-10-05",
  "items": [
    { "id": 1, "type": "EXPENSE" },
    { "id": 3, "type": "INCOME" }
  ],
  "recurrence_scope": "THIS_ONLY"
}
```

| Campo | Obrigatório | Regra |
|-------|-------------|--------|
| date | sim | Data do dia aberto no diálogo |
| items | sim | ≥ 1; `type` = `INCOME` ou `EXPENSE`; `id` do registro-base |
| recurrence_scope | se algum item for recorrente ou projeção naquela data | `THIS_ONLY` \| `THIS_AND_FUTURE` \| `ALL` |

**Response**: `204 No Content`

**Errors**:
| Código | Quando |
|--------|--------|
| 400/422 | `items` vazio; tipo inválido; fatura sintética; `recurrence_scope` ausente quando necessário |
| 404 | id inexistente ou de outro usuário |
| 422 | item não ocorre na `date` informada (ex.: id de outra data) |

**Semântica de escopo** (um valor para todos os recorrentes/projeções do lote):
- `THIS_ONLY` — cancela ou remove só a ocorrência em `date` (exceção `CANCELLED` se for projeção; delete do registro persistido se `date` for a data-base e não houver outras razões para manter)
- `THIS_AND_FUTURE` — encerra a série a partir de `date`
- `ALL` — remove a série
- Itens **não** recorrentes do mesmo lote: sempre exclusão simples do registro, ignorando o escopo

Deletes pontuais `DELETE /income/{id}` e `DELETE /expense/{id}` **permanecem**; a tela de histórico deixa de chamá-los.

Relatórios (`GET /reports`, `GET /reports/export`) **não** passam a listar dias vazios.
