# Research: Edição no histórico, gráfico de evolução, login e documentação

**Feature**: 005-edit-mobile-docs | **Date**: 2026-10-11

## 1. Validação de e-mail e Enter no login

**Decision**: Manter `Validators.email` + `required`; `trim()` no valor antes de validar/enviar; mensagem visível no campo quando `email.invalid && (dirty || submit)`. O `<form (ngSubmit)>` já dispara com Enter; garantir `type="submit"` no botão e que `form.invalid` bloqueia o POST. Placeholder `voce@email.com` permanece.

**Rationale**: O Angular já tem e-mail e ngSubmit. O gap é feedback visível (hoje o botão só fica `disabled`) e espaços. Não inventar máscara que quebra `+` e subdomínios.

**Alternatives considered**: Máscara caractere a caractere — rejeitada. Só validar no servidor — a spec pede bloqueio na tela (SC-001).

## 2. Prefill da edição (GET por id)

**Decision**: `GET /api/income/{id}` e `GET /api/expense/{id}` (JWT, dono). Resposta = DTO atual de create (`IncomeResponse` / `ExpenseResponse`) **ampliado** com ids de fonte/categoria/pagamento e flag de recorrência, para o form não depender só do rótulo do histórico.

**Rationale**: `HistoryEntry` não tem `description` nem ids. Sem GET, o modal não cumpre FR-004.

**Alternatives considered**: Inflar cada linha do `GET /history` — payload maior em todo mês. Duplicar campos no item de Gerenciar — ainda incompleto.

## 3. Mesmo tipo vs troca de tipo

**Decision**:
- Mesmo tipo: `PUT /api/income/{id}` ou `PUT /api/expense/{id}` com o body atual + `recurrence_scope` se `is_recurring`.
- Troca de tipo: `DELETE` do original (com o mesmo `recurrence_scope`) e em seguida `POST` do tipo novo (campos comuns). Não há endpoint “converter”.

**Rationale**: PUT já existe e `RecurrenceEditService.handleEdit` já exige escopo. Tipo é identidade de tabela; substituir (spec) = apagar + criar.

**Alternatives considered**: `POST /api/history/conversions` — uma API nova para um caso. UPDATE in-place entre tabelas — transação frágil.

## 4. Alcance da recorrência na edição

**Decision**: Reusar `RecurrenceScopeDialogComponent` (THIS_ONLY / THIS_AND_FUTURE / ALL) **depois** de Confirmar/Salvar no modal de edição, só se `is_recurring` ou se a pessoa marcou repetir numa edição que já era série. Cancelar o diálogo não chama PUT/DELETE/POST.

**Rationale**: Clarificação C / FR-006a. O diálogo já está no histórico na exclusão.

**Alternatives considered**: Escopo só na exclusão — rejeitado na clarificação.

## 5. Gráfico do relatório (diário + saldo corrido)

**Decision**: Depois de `GET /api/reports`, pedir `GET /api/history?from={period.from}&to={period.to}` (já preenche todos os dias e projeções). Chart.js linha com três datasets: `row.daily_income`, `row.daily_expense_confirmed + daily_expense_pending`, `row.running_balance`. Substitui os três datasets **acumulados** atuais no **mesmo** canvas. Totais KPI, categorias e CSV inalterados. Eixo Y pode ser único (valores no mesmo gráfico; saldo em outra ordem de grandeza é aceitável pela spec).

**Rationale**: Clarificação A. O histórico 002 já é a fonte de saldo projetado e totais do dia. Evita mudar o contrato de `projections.cumulative`.

**Alternatives considered**: Estender `ReportResponse.Point` com `daily` e `balance` — duplicaria a lógica do HistoryService. Dois eixos Y — polish opcional, não bloqueia SC-004.

## 6. Responsividade iPhone 16 / 16 Pro Max

**Decision**: Breakpoint CSS `max-width: 480px` (cobre 393 CSS px do 16 e ~440 do 16 Pro Max). Layout: `.shell` em coluna; `aside` vira barra superior com nav em wrap; `.sheet` com `overflow-x: auto` só no cartão; modais `max-height: 90dvh; overflow: auto`; botões da linha do histórico em wrap. Viewport meta já no `index.html` se presente; senão acrescentar.

**Rationale**: FR-009 pede esses dois retratos, não um app nativo. 480px pega os dois sem media query por aparelho.

**Alternatives considered**: Angular CDK breakpoint — extra. App nativo — fora de escopo.

## 7. Documentação e padrão futuro

**Decision**:
- `README.md` — o que o produto faz (telas, regras visíveis, 001–005).
- `app-settings.md` — Java/Node/MySQL, `mvn` em `backend/`, `npm start` em `frontend/`, portas 8080/4200, variáveis de banco **sem** senha real.
- `RELEASE-NOTES.md` — seções 001–005 em português.
- Em `.specify/memory/constitution.md`, um princípio curto: toda implementação Spec Kit atualiza `RELEASE-NOTES.md` no polish.

**Rationale**: FR-010–013. Constituição hoje é template vazio; este é o gancho “por padrão”.

**Alternatives considered**: Só uma linha no README — a spec pede arquivo próprio de notas. Hook Spec Kit em `extensions.yml` — não há extensões no repo; constituição + tarefa de polish bastam.
