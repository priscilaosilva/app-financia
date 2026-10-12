# Tasks: Modais de nova entrada e nova saída no histórico

**Input**: Design documents from `specs/004-history-entry-modals/`

**Prerequisites**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/ui-contract.md](./contracts/ui-contract.md) · [contracts/api-contract.md](./contracts/api-contract.md) · [quickstart.md](./quickstart.md)

**Stack**: Angular 17 (histórico + forms) · Spring Boot inalterado (`POST /api/income`, `POST /api/expense`, `GET /api/history`, `GET /api/balance`)

**Tests**: não solicitados na spec — omitidos

**Total tasks**: 10

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: paralelo (arquivos diferentes, sem dependência incompleta)
- **[US#]**: user story da spec (US1–US2)
- Sem label = Setup, Foundational ou Polish

---

## Phase 1: Setup

**Purpose**: Confirmar que a feature só muda o histórico Angular; API e schema ficam iguais.

- [X] T001 Confirmar que `POST /api/income` e `POST /api/expense` já existem em `backend/src/main/java/com/financia/income/IncomeController.java` e `backend/src/main/java/com/financia/expense/ExpenseController.java`, e que o overlay de Gerenciar em `frontend/src/app/history/day-entries-dialog.component.ts` é o padrão visual a reusar (sem Flyway nem endpoint novo)

---

## Phase 2: Foundational (bloqueia as stories)

**Purpose**: Formulários aceitam data inicial e emitem a data salva; o histórico tem **um** overlay de registro (`kind: 'income' | 'expense' | null`) no padrão Gerenciar.

**⚠️ CRÍTICO**: Sem o overlay e os Inputs, US1 (topo) e US2 (linha) não têm onde abrir o form.

- [X] T002 [P] Em `frontend/src/app/income/income-form/income-form.component.ts`: `@Input() initialDate` (string `YYYY-MM-DD` ou vazio); no init, `date` começa com `initialDate` (vazio = branco); `date` permanece `Validators.required`; `@Output() saved` emite a `date` do POST (não `void`); sucesso HTTP não fecha o modal (só emite); erro HTTP **não** reseta o form — `frontend/src/app/income/income-form/income-form.component.ts`
- [X] T003 [P] Mesmo contrato de T002 para saída (`initialDate`, `saved` emite `date`, required na data, erro não apaga campos) — `frontend/src/app/expense/expense-form/expense-form.component.ts`
- [X] T004 No histórico, substituir os cards inline (`*ngIf="showIncome"` / `showExpense`) por overlay `position: fixed` (z-index > Gerenciar, p.ex. 30): estado `entryModal: 'income' | 'expense' | null` (no máximo um não-nulo); `*ngIf` recria o form a cada abertura; botão Fechar/Cancelar e `document:keydown.escape` fecham sem POST e **sem** mudar o mês; clique no overlay **não** fecha nem descarta o form; no `saved`, fechar o modal, ajustar `this.month` para o 1º dia civil do mês da data emitida (mesmo recorte das setas) e `reload()` de `GET /history` + `GET /balance` — `frontend/src/app/history/history.component.ts` e `frontend/src/app/history/history.component.html`

**Checkpoint**: Overlay abre/fecha; Escape e Fechar não gravam; fundo não fecha; save já navega o mês (mesmo sem atalhos da linha).

---

## Phase 3: User Story 1 — Registrar pelo topo, com data em branco (P1) 🎯 MVP

**Goal**: **Nova entrada** / **Nova saída** no topo abrem o modal sobre a planilha com data vazia; validação atual impede save sem data; sucesso mostra o lançamento no mês da data.

**Independent Test**: Quickstart Cenário A e D (topo + um modal + validação). Cenário C (outro mês) já coberto pelo `saved` da T004.

- [X] T005 [US1] **Nova entrada** abre `entryModal='income'` com `initialDate=''`; **Nova saída** abre `'expense'` com `initialDate=''`; abrir um fecha o outro; a tabela **não** ganha card acima que empurre a planilha — `frontend/src/app/history/history.component.html` e `frontend/src/app/history/history.component.ts`

**Checkpoint**: Quickstart A (abrir/cancelar/Escape/fundo/salvar) e D (trocar entrada→saída; save sem data bloqueado).

---

## Phase 4: User Story 2 — Registrar a partir da linha do dia, com a data preenchida (P1)

**Goal**: Toda linha do período (com ou sem movimento) tem atalho compacto de entrada e de saída; o modal abre com `row.date`; **Gerenciar** continua quando houver itens apagáveis.

**Independent Test**: Quickstart Cenário B (linha com movimento e linha vazia).

- [X] T006 [US2] Na coluna de ações de cada `HistoryRow`, atalhos compactos (`btn ghost`) de nova entrada e nova saída visíveis **sempre** (inclusive `empty-day`); **Gerenciar** permanece só se `hasDeletable(row)` — `frontend/src/app/history/history.component.html`
- [X] T007 [US2] Atalho da linha abre o mesmo overlay com `initialDate = row.date` (`YYYY-MM-DD`); a pessoa pode alterar a data no form (POST usa o campo, não a linha original); abrir atalho fecha o outro tipo de modal de registro — `frontend/src/app/history/history.component.ts` e `frontend/src/app/history/history.component.html`

**Checkpoint**: Quickstart B; Gerenciar ainda lista/apaga o dia.

---

## Phase 5: Polish

**Purpose**: Validação ponta a ponta e isolamento de outras telas.

- [X] T008 Executar cenários A–D de [quickstart.md](./quickstart.md) (UI autenticada) e registrar o resultado neste `tasks.md`

  **Resultado (2026-10-11):** UI autenticada em `/history`. A: **Nova entrada** abre overlay “Registrar entrada” com data vazia; clique no fundo mantém o modal; Escape fecha sem gravar. C: salvar saída em 2026-11-05 com o mês em outubro → rótulo **novembro de 2026**, linha 05 com 12.34 e **Gerenciar**. B: atalho **Entrada** na linha 07 (vazia) abre data `2026-11-07`; salvar 40 aparece na linha com Gerenciar; Gerenciar lista “Entrada · Lançamento · 40.00”. D: **Nova entrada** depois **Nova saída** deixa só “Registrar saída”; **Salvar saída** desabilitado sem data.
- [X] T009 [P] Confirmar que relatórios, configurações, perfil e os diálogos `frontend/src/app/history/day-entries-dialog.component.ts`, `frontend/src/app/history/confirm-delete-dialog.component.ts` e `frontend/src/app/shared/components/recurrence-scope-dialog/recurrence-scope-dialog.component.ts` não mudam contrato nesta feature — `frontend/src/app/reports/`, `frontend/src/app/settings/`, `frontend/src/app/profile/`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (T001)**: imediato
- **Foundational (T002–T004)**: T002 e T003 em paralelo; T004 depois dos dois (usa os Inputs/Outputs)
- **US1 (T005)**: depois de T004 — MVP
- **US2 (T006–T007)**: depois de T004; na prática depois de T005 para não conflitar no mesmo HTML
- **Polish**: depois das stories

### User Story Dependencies

- **US1 (P1)**: Foundational; T005 liga o topo
- **US2 (P1)**: Foundational; T006 HTML dos atalhos; T007 handlers com `row.date`

### Parallel Opportunities

- T002 e T003 (income vs expense form)
- T009 em paralelo com T008 no polish
- US2 não é paralela à US1 no mesmo `history.component.html` (mesmo arquivo)

---

## Parallel Example: Foundational

```text
Task: "initialDate + saved(date) em frontend/src/app/income/income-form/income-form.component.ts"
Task: "initialDate + saved(date) em frontend/src/app/expense/expense-form/expense-form.component.ts"
```

---

## Implementation Strategy

### MVP (US1)

1. Setup T001  
2. Foundational T002–T004  
3. US1 T005  
4. Validar Quickstart A/C/D e parar se for demo só do modal do topo

### Incremental

1. US1 → registro pelo topo em overlay, data em branco, jump de mês  
2. US2 → atalhos na linha com data preenchida  
3. Polish / quickstart A–D

### Suggested MVP scope

**T001–T005** (modal do topo). Atalhos da linha (US2) no segundo incremento.

---

## Independent test criteria (resumo)

| Story | Como testar sozinha |
|-------|---------------------|
| US1 | Cenários A, C, D: topo com data vazia; fundo não fecha; Escape fecha; save vai ao mês da data |
| US2 | Cenário B: atalho na linha cheia e vazia; data pré-preenchida; Gerenciar permanece |

---

## Notes

- Formato checklist: checkbox + T00x + [P]? + [US#]? + descrição com path
- Sem tarefas de teste automatizado (não pedidas)
- Sem migração SQL e sem controller novo
- Clique no overlay nunca fecha (FR-007); `kind` único (FR-008)
