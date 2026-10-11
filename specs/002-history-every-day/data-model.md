# Data Model: Histórico dia a dia e exclusão em lote

**Feature**: 002-history-every-day | **Date**: 2026-10-05

Nenhuma tabela nova. Entidades persistidas continuam as de `001-finance-management` (`income_entries`, `expense_entries`, `recurrence_rules`, `recurrence_exceptions`).

## Entidades de visão (somente leitura)

### Dia do histórico (`HistoryDay`)

Representa uma data civil no intervalo `from`–`to`. **Não persistido.**

| Campo | Tipo | Regras |
|-------|------|--------|
| date | date | Obrigatório; único no array `rows` |
| entries | lista de lançamentos | Pode ser vazia |
| daily_income | decimal | Soma das entradas do dia (0 se vazio) |
| daily_expense_confirmed | decimal | Saídas confirmadas + fatura sintética (0 se vazio) |
| daily_expense_pending | decimal | Pendentes de cartão (0 se vazio) |
| running_balance | decimal | Saldo ao **fim** do dia; se `entries` vazio, igual ao dia anterior (ou saldo de abertura se for o primeiro dia) |
| has_movement | boolean (opcional) | `true` se `entries.length > 0` |

**Invariantes**:
- Número de dias = `to - from + 1` (inclusive)
- Ordem crescente por `date`
- Sem buracos de calendário

### Lançamento do dia (`HistoryEntry`)

Igual ao contrato 001, com regra extra de apagabilidade:

| Campo | Apagável no modal? |
|-------|-------------------|
| type = INCOME ou EXPENSE e id ≠ null | Sim |
| type = CREDIT_CARD_BILL | Não |
| is_recurring_projection = true | Sim (cancela/modifica a ocorrência em `date` via exceção/escopo) |

## Entidades de comando (exclusão)

### Seleção para exclusão (`DayDeletion`)

Não persistida; payload de um clique em “Apagar”.

| Campo | Tipo | Regras |
|-------|------|--------|
| date | date | Obrigatório; todos os itens pertencem a este dia |
| items | lista | ≥ 1 após validação de seleção; cada item `{ id: long, type: INCOME \| EXPENSE }` |
| recurrence_scope | enum | Obrigatório se **qualquer** item for recorrente ou projeção; ignorado se nenhum for. Valores: `THIS_ONLY` \| `THIS_AND_FUTURE` \| `ALL` |

**Transições**:
1. Cliente envia lote após “tem certeza?” (e escopo, se couber)
2. Servidor, em uma transação: para cada item, aplica delete/exceção na `date` informada
3. Sucesso → 204; o cliente recarrega o histórico
4. Item inválido (id inexistente, tipo fatura, outro usuário, data que não corresponde à ocorrência) → 404/422 e rollback do lote

### Recurrence exception (já existente)

`THIS_ONLY` numa projeção na data D grava `recurrence_exceptions.original_date = D` com tipo `CANCELLED` (exclusão) — não usa a data-base do registro persistido.

## Validações

- `from`/`to` obrigatórios e `to >= from` (já existente)
- Lote: `items` não vazio; ids únicos por `(id, type)`
- Não aceitar `CREDIT_CARD_BILL`
- Isolamento: só lançamentos do usuário autenticado
