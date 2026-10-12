# API contract: Edição e séries diárias no relatório

**Feature**: 005-edit-mobile-docs | **Date**: 2026-10-11

JWT em todas as rotas abaixo. SNAKE_CASE.

## Novos: obter lançamento para edição

```http
GET /api/income/{id}
GET /api/expense/{id}
```

**200** — body alinhado ao create, mais ids e recorrência, por exemplo:

```json
{
  "id": 12,
  "date": "2026-11-07",
  "amount": 40.00,
  "description": "…",
  "income_source_id": 3,
  "is_recurring": false
}
```

Despesa: `expense_category_id`, `payment_method_id` no lugar da fonte. **404** se não for do usuário.

## Existentes: gravar edição (mesmo tipo)

```http
PUT /api/income/{id}
PUT /api/expense/{id}
```

Body = request de create + `recurrence_scope` (`THIS_ONLY` \| `THIS_AND_FUTURE` \| `ALL`) quando `is_recurring`. **200**. **422** se recorrente e o escopo faltar.

## Existentes: troca de tipo

```http
DELETE /api/income/{id}     { "recurrence_scope": "THIS_ONLY" }
POST   /api/expense         { "date", "amount", "description", … }
```

(e o simétrico saída → entrada). DELETE **204**; POST **201**.

## Relatório + histórico (sem mudança de path)

```http
GET /api/reports?filter=MONTH&year=2026&month=11
GET /api/history?from={period.from}&to={period.to}
```

O gráfico usa as **linhas** do histórico (`daily_income`, despesas do dia, `running_balance`). `projections.*.cumulative` do relatório pode permanecer para CSV/outros; a UI do canvas passa a usar o histórico.

## Sem mudança

Auth forgot-password, day-deletions, balance.
