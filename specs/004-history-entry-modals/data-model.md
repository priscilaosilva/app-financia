# Data Model: Modais de nova entrada e nova saída no histórico

**Feature**: 004-history-entry-modals | **Date**: 2026-10-11

Sem tabelas novas. Reusa lançamentos, histórico por dia e saldo já persistidos (001/002). Abaixo: estado de UI e o payload de criação inalterado.

## Entidades de UI (não persistidas)

### Modal de registro

| Campo | Tipo | Regras |
|-------|------|--------|
| kind | `'income'` \| `'expense'` \| `null` | No máximo um não-nulo (FR-008). `null` = fechado |
| initialDate | string (`YYYY-MM-DD`) ou vazio | Topo → vazio. Linha → `HistoryRow.date`. Não muda sozinho depois de aberto |
| date (no form) | string | Obrigatória para salvar (validators atuais). Editável mesmo se veio da linha |
| demais campos | iguais aos forms atuais | Valor, fonte/categoria, pagamento, descrição, recorrência |

**Transições**:

```text
fechado
  -- Nova entrada (topo) --> aberto income, date=''
  -- Nova saída (topo)    --> aberto expense, date=''
  -- atalho entrada linha --> aberto income, date=row.date
  -- atalho saída linha   --> aberto expense, date=row.date
  (abrir um tipo fecha o outro)

aberto
  -- Fechar / Cancelar / Escape --> fechado; planilha e mês inalterados; nada POST
  -- clique no overlay          --> permanece aberto (sem transição)
  -- submit inválido            --> permanece aberto; mensagem/disabled do form
  -- POST 201                   --> fechado; month = mês da date salva; reload histórico+saldo
```

Clique no fundo **não** é transição. Falha HTTP: permanece aberto; campos não são resetados.

### Linha de data

Já existe como `HistoryRow` (`GET /history`).

| Campo relevante | Uso nesta feature |
|-----------------|-------------------|
| date | Prefill do atalho; alvo do destaque após salvar |
| has_movement / entries | Atalhos existem mesmo sem movimento; **Gerenciar** só com itens apagáveis |

Relação: 1 linha → 2 atalhos (entrada e saída). Não cria entidade persistida.

### Lançamento

Inalterado. Criado via `IncomeRequest` / `ExpenseRequest` existentes. Recorrência, cartão pendente e saldo seguem as regras atuais.

## Entidade persistida (referência, sem mudança de schema)

### Entrada / saída

| Campo | Regras (já vigentes) |
|-------|----------------------|
| date | Obrigatória; ISO date |
| amount | > 0 |
| income_source_id / expense_category_id / payment_method_id | Opcionais |
| description | Opcional |
| recurrence | Opcional; `start_date` = date do form |

Isolamento por usuário autenticado (JWT da 003). Sem `token_version` nesta feature.

## Validação (cliente, iguais às atuais)

- Data vazia (modal do topo sem preencher) → submit bloqueado (`Validators.required`)
- Valor ausente ou ≤ 0 → bloqueado
- Servidor continua sendo a fonte da verdade no POST (400/422 não fecha o modal)
