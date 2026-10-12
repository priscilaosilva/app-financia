# Tasks: Edição no histórico, gráfico de evolução, login e documentação

**Input**: Design documents from `specs/005-edit-mobile-docs/`

**Prerequisites**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/api-contract.md](./contracts/api-contract.md) · [contracts/ui-contract.md](./contracts/ui-contract.md) · [quickstart.md](./quickstart.md)

**Stack**: Angular 17 · Spring Boot 3.3.4 · Chart.js · `GET /history` para o gráfico · GET por id novo · sem Flyway

**Tests**: não solicitados na spec — omitidos

**Total tasks**: 20

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: paralelo (arquivos diferentes, sem dependência incompleta)
- **[US#]**: user story da spec (US1–US5)
- Sem label = Setup, Foundational ou Polish

---

## Phase 1: Setup

**Purpose**: Confirmar o que já existe (PUT/DELETE, forms 004, Chart.js, login com `Validators.email`).

- [X] T001 Confirmar `PUT`/`DELETE` e ausência de `GET /{id}` em `backend/src/main/java/com/financia/income/IncomeController.java` e `backend/src/main/java/com/financia/expense/ExpenseController.java`; confirmar overlay 004 em `frontend/src/app/history/history.component.html` e `Validators.email` em `frontend/src/app/auth/login/login.component.ts`

---

## Phase 2: Foundational (bloqueia US2)

**Purpose**: Prefill da edição exige GET autenticado do lançamento (dono). US1/US3–US5 não dependem disto, mas US2 sim.

**⚠️ CRÍTICO**: Sem GET, o modal de edição não preenche descrição nem ids de fonte/categoria/pagamento.

- [X] T002 [P] `GET /api/income/{id}` 200 com `id`, `date`, `amount`, `description`, `income_source_id`, `is_recurring`; 404 se não for do usuário — `backend/src/main/java/com/financia/income/IncomeController.java`, `IncomeService.java`, `IncomeResponse.java`
- [X] T003 [P] `GET /api/expense/{id}` 200 com `id`, `date`, `amount`, `description`, `expense_category_id`, `payment_method_id`, `is_recurring`; 404 se não for do usuário — `backend/src/main/java/com/financia/expense/ExpenseController.java`, `ExpenseService.java`, `ExpenseResponse.java`

**Checkpoint**: `GET` com JWT do dono devolve o lançamento; outro usuário 404.

---

## Phase 3: User Story 1 — E-mail válido e tecla Enter (P1) 🎯 MVP

**Goal**: E-mail fora do padrão bloqueia envio (login, cadastro, forgot) com erro visível; Enter no form válido tenta entrar como **Entrar**. Trim nos espaços.

**Independent Test**: Quickstart Cenário A.

- [X] T004 [US1] No form de login: `email` continua `Validators.required` + `Validators.email`; aplicar trim no valor; botão submit `[disabled]` se `email` inválido; mensagem no campo se inválido (`dirty` ou após submit) — `frontend/src/app/auth/login/login.component.ts` e `frontend/src/app/auth/login/login.component.html`
- [X] T005 [US1] Garantir `(ngSubmit)` + botão `type="submit"`: Enter em e-mail/senha com form válido dispara o mesmo `submit()` do modo atual; form inválido **não** chama API — `frontend/src/app/auth/login/login.component.html` e `frontend/src/app/auth/login/login.component.ts`

**Checkpoint**: Quickstart A (e-mail inválido não envia; Enter no válido tenta login).

---

## Phase 4: User Story 2 — Editar a partir de Gerenciar (P1)

**Goal**: **Editar** só com 1 item; modal 004 preenchido; troca entrada ↔ saída preservando data/valor/descrição; mesmo tipo PUT; outro tipo DELETE+POST; recorrente pede alcance; Gerenciar permanece (fecha se o dia ficar sem apagáveis).

**Independent Test**: Quickstart B e C.

- [X] T006 [US2] Em Gerenciar, botão **Editar** visível só se exatamente 1 item marcado; emitir o item — `frontend/src/app/history/day-entries-dialog.component.ts`
- [X] T007 [P] [US2] `getIncome(id)` → `GET /api/income/{id}` em `frontend/src/app/income/income.service.ts`
- [X] T008 [P] [US2] `getExpense(id)` → `GET /api/expense/{id}` em `frontend/src/app/expense/expense.service.ts`
- [X] T009 [US2] Forms de entrada/saída: modo edição (`id` + valores iniciais); controle de tipo só na edição; ao trocar tipo preservar `date`, `amount`, `description` e limpar campos exclusivos (fonte vs categoria/pagamento) — `frontend/src/app/income/income-form/income-form.component.ts`, `frontend/src/app/expense/expense-form/expense-form.component.ts` e templates (ou um wrapper no histórico)
- [X] T010 [US2] Abrir overlay de edição (z-index > Gerenciar) com GET prefill; Fechar/Escape/fundo iguais à 004; Salvar mesmo tipo → `PUT` com `recurrence_scope` se `is_recurring`; outro tipo → `DELETE` (escopo) + `POST`; se recorrente, `RecurrenceScopeDialogComponent` **antes** do HTTP; cancelar o alcance **não** grava — `frontend/src/app/history/history.component.ts` e `frontend/src/app/history/history.component.html`
- [X] T011 [US2] Após sucesso: fechar só a edição; `reload(true)` da planilha; lista de Gerenciar do **mesmo** `date` atualizada e seleção limpa; se não houver apagáveis, fechar Gerenciar — `frontend/src/app/history/history.component.ts`

**Checkpoint**: Quickstart B e C.

---

## Phase 5: User Story 3 — Gráfico saldo / entradas do dia / saídas do dia (P2)

**Goal**: Canvas do relatório com três linhas no período do filtro; entradas/saídas **diárias**; saldo **corrido** (projeção futura via histórico).

**Independent Test**: Quickstart D.

- [X] T012 [US3] Após `GET /reports`, buscar `GET /history?from=&to=` do `period` e plotar Chart.js `line` com `daily_income`, `daily_expense_confirmed + daily_expense_pending`, `running_balance` (não `projections.*.cumulative`); KPI/categorias/CSV intactos — `frontend/src/app/reports/reports.component.ts` e `frontend/src/app/reports/reports.component.html`

**Checkpoint**: Quickstart D (pico no dia do lançamento, não rampa acumulada).

---

## Phase 6: User Story 4 — iPhone 16 e 16 Pro Max (P2)

**Goal**: Retrato ~393px e ~440px sem scroll horizontal da página; nav utilizável; tabela e modais roláveis por dentro.

**Independent Test**: Quickstart E.

- [X] T013 [US4] Em `max-width: 480px`, `.shell` em coluna e `aside` como barra superior com nav em wrap — `frontend/src/styles.scss` e `frontend/src/app/layout/app-layout.component.html`
- [X] T014 [US4] Histórico: overflow-x só no card da tabela; botões da linha em wrap; overlays `max-height: 90dvh` e scroll interno — `frontend/src/styles.scss` e `frontend/src/app/history/history.component.html` (e estilos dos diálogos em `frontend/src/app/history/day-entries-dialog.component.ts` se preciso)
- [X] T015 [P] [US4] Confirmar/acrescentar `viewport` em `frontend/src/index.html` para largura do dispositivo

**Checkpoint**: Quickstart E nas duas larguras.

---

## Phase 7: User Story 5 — README, app-settings, release notes (P3)

**Goal**: README funcional; execução em `app-settings.md`; `RELEASE-NOTES.md` 001–005; constituição exige atualizar notas no polish das próximas features.

**Independent Test**: Quickstart F.

- [X] T016 [P] [US5] Criar `app-settings.md` na raiz com pré-requisitos e como rodar (`mvn` em `backend/`, `npm start` em `frontend/`, portas 8080/4200); **sem** senha real de banco — `app-settings.md`
- [X] T017 [P] [US5] Reescrever `README.md` como documentação funcional do Financia (telas e regras visíveis; apontar `app-settings.md` e `RELEASE-NOTES.md`) — `README.md`
- [X] T018 [US5] Criar `RELEASE-NOTES.md` na raiz, português, seções das features 001–005 — `RELEASE-NOTES.md`
- [X] T019 [US5] Acrescentar princípio: implementação Spec Kit atualiza `RELEASE-NOTES.md` no polish — `.specify/memory/constitution.md`

**Checkpoint**: Quickstart F.

---

## Phase 8: Polish

**Purpose**: Validação ponta a ponta.

- [X] T020 Executar cenários A–F de [quickstart.md](./quickstart.md) e registrar o resultado neste `tasks.md`

### T020 — resultado (2026-10-11)

| Cenário | Resultado |
|---------|-----------|
| A | PASS — `nao-e-email` mostra erro, **Entrar** desabilitado, permanece em `/login` após Enter |
| B | PASS — **Editar** só com 1 item; modal preenchido; valor 50→80; Gerenciar aberto com 80; troca para entrada (saldo +80) |
| C | PASS — diálogo de alcance na edição recorrente; **Cancelar** mantém o modal de edição |
| D | PASS — canvas no relatório após `GET /history`; séries saldo / entradas do dia / saídas do dia |
| E | PASS — 393×852 e 440×956 sem overflow horizontal da página; nav `row` + `wrap` |
| F | PASS — README funcional, `app-settings.md`, `RELEASE-NOTES.md` 001–005 |

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (T001)**: imediato
- **Foundational (T002–T003)**: paralelo; bloqueia US2
- **US1 (T004–T005)**: depois de T001 (não precisa dos GET)
- **US2 (T006–T011)**: depois de T002–T003; T007/T008 [P]; T009 antes de T010; T011 depois de T010
- **US3 (T012)**: independente da US2
- **US4 (T013–T015)**: depois das UIs que altera (idealmente depois de US2/US3)
- **US5 (T016–T019)**: independente do código; T016/T017 [P]; T018 antes ou com T019
- **Polish (T020)**: depois das stories a validar

### User Story Dependencies

- **US1**: só Setup
- **US2**: Foundational GET
- **US3**: Setup (history API já existe)
- **US4**: convive com US2/US3 nos mesmos HTML/CSS
- **US5**: só arquivos na raiz / constituição

### Parallel Opportunities

- T002 ∥ T003
- T007 ∥ T008
- T016 ∥ T017
- US1 ∥ Foundational ∥ US3 (arquivos diferentes)
- US5 ∥ qualquer story de código

---

## Parallel Example: Foundational + US1

```text
Task: "GET /api/income/{id} em backend/.../income/"
Task: "GET /api/expense/{id} em backend/.../expense/"
Task: "trim + Validators.email visível em frontend/.../login/"
```

---

## Implementation Strategy

### MVP (US1)

1. T001  
2. T004–T005 (login)  
3. Validar Quickstart A  

### Incremental

1. US1 → e-mail/Enter  
2. Foundational + US2 → edição  
3. US3 → gráfico  
4. US4 → mobile  
5. US5 + T020 → docs e quickstart A–F  

### Suggested MVP scope

**T001 + T004–T005**. Edição (US2) é o segundo incremento de produto.

---

## Independent test criteria (resumo)

| Story | Como testar sozinha |
|-------|---------------------|
| US1 | Cenário A |
| US2 | Cenários B e C |
| US3 | Cenário D |
| US4 | Cenário E (393×852 e 440×956) |
| US5 | Cenário F |

---

## Notes

- Formato checklist: checkbox + T00x + [P]? + [US#]? + path
- Sem testes automatizados
- Sem migração SQL
- Troca de tipo = DELETE + POST; gráfico **não** usa `cumulative`
