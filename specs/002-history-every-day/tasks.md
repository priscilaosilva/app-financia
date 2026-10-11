# Tasks: Histórico dia a dia e exclusão em lote

**Input**: Design documents from `specs/002-history-every-day/`

**Prerequisites**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/api-contract.md](./contracts/api-contract.md) · [quickstart.md](./quickstart.md)

**Stack**: Java 17 + Spring Boot 3.3.4 · Angular 17 · MySQL 8.x (sem migration nova)

**Tests**: não solicitados na spec — omitidos

**Total tasks**: 21

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: paralelo (arquivos diferentes, sem dependência incompleta)
- **[US#]**: user story da spec (US1–US4)
- Sem label = Setup, Foundational ou Polish

---

## Phase 1: Setup

**Purpose**: Confirmar que a feature reusa o app 001, sem banco novo.

- [X] T001 Confirmar que não haverá Flyway novo (dias vazios não persistidos) e que `GET /history` permanece em `backend/src/main/java/com/financia/history/HistoryController.java`

---

## Phase 2: Foundational (bloqueia as stories)

**Purpose**: `GET /history` passa a devolver uma linha por dia civil do intervalo (`rows.length = to - from + 1`), com `running_balance` carregado e `entries` podendo ser lista vazia.

**⚠️ CRÍTICO**: Stories de UI e exclusão assumem esse contrato.

- [X] T002 Em `HistoryService.history`, após montar lançamentos por data, percorrer cada dia de `from` a `to` inclusive; dias sem mapa recebem `entries` vazio, `daily_income`/`daily_expense_confirmed`/`daily_expense_pending` = 0, `running_balance` igual ao acumulado anterior (saldo de abertura no primeiro dia) — `backend/src/main/java/com/financia/history/HistoryService.java`
- [X] T003 [P] Incluir `has_movement` (boolean, `true` se `entries` não vazio) em `HistoryRowDto` — `backend/src/main/java/com/financia/history/dto/HistoryRowDto.java`

**Checkpoint**: `GET /api/history?from=&to=` retorna N linhas para N dias (quickstart Cenário A, só API).

---

## Phase 3: User Story 1 — Linha para cada dia do período (P1) 🎯 MVP

**Goal**: A planilha mostra todos os dias do mês visível, com totais e lista de lançamentos do dia, sem pular datas.

**Independent Test**: Quickstart Cenário A na UI: 10 dias, movimento em 2, 10 linhas, saldo estável nos vazios.

- [X] T004 [US1] Remover o empty-state “Nenhum lançamento neste período” quando `rows` já cobre o calendário; só esconder a tabela se `history` ainda não carregou — `frontend/src/app/history/history.component.html`
- [X] T005 [US1] Garantir que a tabela continua listando `entry.label` / pendente / projeção **dentro da linha do dia**, junto das colunas Entrada/Saída/Saldo — `frontend/src/app/history/history.component.html`

**Checkpoint**: US1 testável só com GET /history + tela; exclusão antiga ainda pode existir.

---

## Phase 4: User Story 4 — Excluir lançamentos pela lista do dia (P1)

**Goal**: Sem botão excluir por item na planilha. Diálogo do dia: seleção → Apagar → tem certeza? → um escopo se houver recorrente → `POST /history/day-deletions`. Diálogo permanece se ainda houver itens; fecha se esvaziar.

**Independent Test**: Quickstart B, C, D, E.

- [X] T006 [P] [US4] Criar DTO `DayDeletionRequest` com `date` obrigatório, `items` (lista `{ id: Long, type: INCOME|EXPENSE }`, ≥ 1 após validação), `recurrence_scope` opcional (`THIS_ONLY` | `THIS_AND_FUTURE` | `ALL`, obrigatório se algum item for recorrente ou projeção na `date`) — `backend/src/main/java/com/financia/history/dto/DayDeletionRequest.java`
- [X] T007 [US4] Estender `ExpenseService.delete` e `IncomeService.delete` para receber `LocalDate occurrenceDate` (além do `id` e `RecurrenceScope`); `THIS_ONLY` em projeção grava exceção `CANCELLED` em `occurrenceDate`, não só na data-base — `backend/src/main/java/com/financia/expense/ExpenseService.java` e `backend/src/main/java/com/financia/income/IncomeService.java`
- [X] T008 [US4] Implementar `HistoryDeletionService`: transação única; rejeitar `items` vazio e tipo diferente de INCOME/EXPENSE (422); rejeitar fatura sintética; 404 se id/usuário inválido; 422 se o item não ocorre em `date`; `recurrence_scope` ausente quando necessário → 422; aplicar o mesmo escopo a todos os recorrentes/projeções do lote; não recorrentes = delete simples — `backend/src/main/java/com/financia/history/HistoryDeletionService.java`
- [X] T009 [US4] Expor `POST /api/history/day-deletions` (204) no `HistoryController` — `backend/src/main/java/com/financia/history/HistoryController.java`
- [X] T010 [P] [US4] Adicionar `deleteDayEntries(date, items, recurrenceScope)` em `frontend/src/app/history/history.service.ts`
- [X] T011 [P] [US4] Criar diálogo de lista do dia (checkboxes, botão Apagar, mensagem se seleção vazia, **sem** “tem certeza?” nesse caso) — `frontend/src/app/history/day-entries-dialog.component.ts` + `.html`
- [X] T012 [P] [US4] Criar confirmação “tem certeza?” (recusar não apaga nada e volta à lista com a seleção intacta) — `frontend/src/app/history/confirm-delete-dialog.component.ts` + `.html`
- [X] T013 [US4] No `HistoryComponent`, remover os botões `excluir` por `entry`; adicionar ação “Gerenciar” na linha só se existir item apagável (`id` não nulo e `type` INCOME|EXPENSE); fluxo: lista → Apagar → confirmação → se houver recorrente/`is_recurring_projection` um `RecurrenceScopeDialog` → POST lote — `frontend/src/app/history/history.component.ts` + `.html`
- [X] T014 [US4] Após 204, recarregar `GET /history` e `GET /balance`; se o dia ainda tiver itens apagáveis, manter o diálogo de lista aberto com dados novos; se não restar nenhum, fechar — `frontend/src/app/history/history.component.ts`

**Checkpoint**: Quickstart B–E na UI; faturas não selecionáveis.

---

## Phase 5: User Story 2 — Distinguir dias com e sem movimento (P2)

**Goal**: Dia vazio reconhecível em menos de 5 segundos; totais zerados; sem lançamentos inventados.

**Independent Test**: Mês misto: linhas `.empty-day` vs linhas com labels.

- [X] T015 [US2] Aplicar classe CSS `empty-day` quando `!row.has_movement` (ou `entries.length === 0`): tipografia/opacidade distintas, entrada/saída vazias ou 0 — `frontend/src/app/history/history.component.html` e `frontend/src/styles.css`
- [X] T016 [US2] Não renderizar rótulos de lançamento em dia vazio; pendente de cartão e fatura **não** usam `empty-day` — `frontend/src/app/history/history.component.html`

**Checkpoint**: SC-003 visualmente verificável.

---

## Phase 6: User Story 3 — Horizonte contínuo passado e futuro (P2)

**Goal**: Setas de mês preenchem todos os dias; futuro vazio carrega saldo projetado; passado sem lançamentos mostra saldo de abertura (0 se nunca houve movimento).

**Independent Test**: Avançar um mês sem recorrência na maior parte dos dias; voltar a um mês anterior a qualquer lançamento.

- [X] T017 [US3] Confirmar que `openingBalance` alimenta o primeiro dia vazio de um período futuro/passado em `HistoryService` (retroativo já recalcula dias seguintes, inclusive vazios) — `backend/src/main/java/com/financia/history/HistoryService.java`
- [X] T018 [US3] Manter navegação ←/→ do `HistoryComponent` recarregando o mês completo (todos os dias civis) — `frontend/src/app/history/history.component.ts`

**Checkpoint**: SC-005/SC-006 no recorte de um mês para frente e para trás.

---

## Phase 7: Polish

**Purpose**: Validação e consistência.

- [X] T019 Executar os cenários A–E de [quickstart.md](./quickstart.md) contra API + UI e registrar resultado neste `tasks.md`

  **Resultado (2026-10-05):** API A/B/D/E OK (`rows.length=10`, 204 exclusão, aluguel permanece em 05/11 e 05/12, `CREDIT_CARD_BILL` → 422). UI A/C/US2/US3: outubro 31 linhas (29 `.empty-day`), só Gerenciar, Apagar sem seleção mostra “Selecione ao menos um registro.” sem “tem certeza?”, recusar confirmação mantém B marcado; novembro 30 dias vazios com saldo carregado 800. Relatório/CSV sem dia 02 vazio.
- [X] T020 [P] Garantir que `ReportService` / CSV **não** passaram a emitir dias vazios — `backend/src/main/java/com/financia/report/ReportService.java`
- [X] T021 [P] Revogar na prática a nota da 001 (“dias sem lançamentos são omitidos”) alinhando comentários em `specs/001-finance-management/contracts/api-contract.md` ou deixando só o contrato 002 como fonte para `/history`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup**: imediato
- **Foundational (T002–T003)**: bloqueia todas as stories
- **US1**: depois da Foundational — MVP
- **US4**: depois da Foundational; melhor depois da US1 (planilha já contínua)
- **US2 / US3**: depois da Foundational; US2 combina com a UI da US1
- **Polish**: depois das stories desejadas

### User Story Dependencies

- **US1 (P1)**: só Foundational
- **US4 (P1)**: Foundational; T007 antes de T008; T011/T012 [P] antes de T013
- **US2 (P2)**: Foundational + US1 (classe na mesma tabela)
- **US3 (P2)**: Foundational; setas já existem, validar preenchimento futuro/passado

### Parallel Opportunities

- T003 paralelo a T002 (arquivos diferentes) após T002 se `has_movement` for preenchido no mesmo service — na prática T003 DTO primeiro, T002 preenche o campo
- T006, T010, T011, T012 em paralelo após Foundational
- T015/T016 (US2) em paralelo com T017/T018 (US3) depois da US1
- T020 e T021 em paralelo no polish

---

## Parallel Example: User Story 4

```text
Task: "DayDeletionRequest em backend/.../history/dto/DayDeletionRequest.java"
Task: "deleteDayEntries em frontend/.../history.service.ts"
Task: "day-entries-dialog.component.ts"
Task: "confirm-delete-dialog.component.ts"
```

---

## Implementation Strategy

### MVP (US1)

1. Setup T001  
2. Foundational T002–T003  
3. US1 T004–T005  
4. Validar Cenário A e parar se for demo do horizonte

### Incremental

1. US1 → horizonte contínuo  
2. US4 → exclusão em lote  
3. US2 → contraste visual  
4. US3 → checagem passado/futuro  
5. Polish / quickstart A–E

### Suggested MVP scope

**T001–T005** (calendário contínuo na API e na planilha). Exclusão em lote (US4) no segundo incremento.

---

## Independent test criteria (resumo)

| Story | Como testar sozinha |
|-------|---------------------|
| US1 | Cenário A: 10 linhas, 2 com movimento, saldo carregado |
| US4 | Cenários B–E: lote, sem seleção, recusa, escopo único, fatura 422 |
| US2 | Mês misto: `empty-day` vs linha com labels |
| US3 | ←/→ um mês; futuro vazio com saldo projetado; passado zerado se sem histórico |

---

## Notes

- Formato checklist: checkbox + T00x + [P]? + [US#]? + descrição com path
- Sem tarefas de teste automatizado (não pedidas)
- `DELETE /income/{id}` e `DELETE /expense/{id}` permanecem; a planilha deixa de chamá-los
