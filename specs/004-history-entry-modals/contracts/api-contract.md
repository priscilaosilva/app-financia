# API contract: Modais de registro no histórico

**Feature**: 004-history-entry-modals | **Date**: 2026-10-11

Nenhum endpoint novo. O cliente autenticado (JWT) continua a usar:

## Criar entrada

```http
POST /api/income
Authorization: Bearer {jwt}
Content-Type: application/json

{
  "date": "2026-10-07",
  "amount": 100.00,
  "income_source_id": null,
  "description": "",
  "recurrence": null
}
```

**201** + corpo de `IncomeResponse` existente. Data obrigatória; validação atual (400/422 não implica fechar o modal no cliente).

`recurrence`, se marcado no form: `{ "frequency": "MONTHLY"|"DAILY"|"YEARLY", "start_date": "<date do form>" }`.

## Criar saída

```http
POST /api/expense
Authorization: Bearer {jwt}

{
  "date": "2026-10-07",
  "amount": 50.00,
  "expense_category_id": null,
  "payment_method_id": null,
  "description": "",
  "recurrence": null
}
```

**201** + `ExpenseResponse`. Cartão de crédito e status pendente seguem a regra já implementada no serviço.

## Atualizar a planilha após sucesso

```http
GET /api/history?from=2026-10-01&to=2026-10-31
GET /api/balance
```

`from`/`to` = primeiro e último dia civis do **mês da data salva** (não necessariamente o mês que estava na tela). Formato de linha inalterado (`date`, `entries`, totais, `has_movement`).

## Não alterar

`POST /api/history/day-deletions`, rotas de auth (003), fontes/categorias/métodos.
