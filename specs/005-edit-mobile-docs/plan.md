# Implementation Plan: Edição no histórico, gráfico de evolução, login e documentação

**Branch**: `005-edit-mobile-docs` | **Date**: 2026-10-11 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/005-edit-mobile-docs/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Login recusa e-mail fora do padrão e Enter envia o form válido. No histórico, Gerenciar ganha **Editar** com um item: modal no estilo 004, prefill via GET, troca de tipo (DELETE+POST) ou PUT no mesmo tipo, alcance de recorrência como na exclusão; Gerenciar permanece aberto. Relatórios passam a plotar saldo corrido e entradas/saídas **do dia** a partir do `GET /history` do período. Layout ≤480px para iPhone 16 / 16 Pro Max. README funcional, `app-settings.md` para rodar, `RELEASE-NOTES.md` 001–005 e princípio na constituição para o próximo ciclo.

## Technical Context

**Language/Version**: TypeScript (Angular 17) + Java 17 (Spring Boot 3.3.4)

**Primary Dependencies**:
- Frontend: Reactive Forms (`Validators.email`), Chart.js, overlays 004, `RecurrenceScopeDialogComponent`
- Backend: `IncomeService`/`ExpenseService` PUT+DELETE já com `recurrence_scope`; GET por id **novo**; `GET /history` para o gráfico

**Storage**: MySQL 8.x — **sem** migração

**Testing**: Manual via [quickstart.md](./quickstart.md)

**Target Platform**: Desktop + retrato iPhone 16 (≈393px) e 16 Pro Max (≈440px)

**Project Type**: Web application — Angular SPA + Spring Boot REST API

**Performance Goals**: Enter no login responde em até 2 s (SC-002); edição em menos de 2 min (SC-003); gráfico no mesmo tempo de gerar relatório hoje

**Constraints**: Troca de tipo = substituir (não dois lançamentos); entradas/saídas do gráfico diárias; Gerenciar aberto após save; docs em português na raiz; futuro polish atualiza release notes

**Scale/Scope**: Cinco user stories (login, edição, gráfico, mobile, docs)

## Constitution Check

*GATE: Constituição do projeto (`constitution.md`) contém apenas o template de exemplo — nenhum gate formal a verificar.*

**Pós-design**: Sem violação. Esta feature **preenche** um princípio de documentação (release notes no ciclo Spec Kit) no template vazio, alinhado a FR-013.

## Project Structure

### Documentation (this feature)

```text
specs/005-edit-mobile-docs/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── api-contract.md
│   └── ui-contract.md
└── tasks.md             # /speckit-tasks — ainda não criado

README.md                # reescrito (funcional)
app-settings.md          # novo (como rodar)
RELEASE-NOTES.md         # novo (001–005)
.specify/memory/constitution.md  # princípio de release notes
```

### Source Code (repository root)

```text
backend/src/main/java/com/financia/
├── income/                 # GET /{id}; PUT/DELETE já existentes
└── expense/                # idem

frontend/src/app/
├── auth/login/             # trim, erro de e-mail, Enter
├── history/                # Editar no day-dialog; modal edição; scope
├── income/ / expense/      # form em modo edição + troca de tipo
├── reports/                # Chart.js com GET /history
└── layout/ + styles.scss   # shell empilhado ≤480px
```

**Structure Decision**: Continua o web app existente. Gráfico reusa histórico; GET por id é o único acréscimo de API.

## Complexity Tracking

Nenhuma violação de constituição.

| Decisão | Alternativa Rejeitada | Motivo |
|---------|----------------------|--------|
| GET por id para prefill | Inflar GET /history | Menos payload no mês |
| Troca de tipo = DELETE+POST | Endpoint de conversão | Reusa exclusão+criação e escopo |
| Chart a partir do histórico | Novos campos em projections | Saldo projetado e dias vazios já existem |
| Breakpoint 480px | Media query por modelo | Cobre 16 e 16 Pro Max |
| Constituição + RELEASE-NOTES.md | Só README | FR-013 pede padrão para o próximo ciclo |
