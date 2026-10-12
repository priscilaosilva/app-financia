# Implementation Plan: Modais de nova entrada e nova saída no histórico

**Branch**: `004-history-entry-modals` | **Date**: 2026-10-11 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/004-history-entry-modals/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

No histórico, **Nova entrada** e **Nova saída** deixam de abrir um card que empurra a planilha: passam a um overlay sobre a tabela. No topo a data começa vazia; em cada linha do calendário há atalhos compactos que abrem o mesmo formulário já com a data do dia. Fechar/Cancelar e Escape saem sem salvar; clique no fundo não fecha. Após sucesso, a planilha mostra o mês da data salva.

Abordagem só no Angular: reusar `IncomeFormComponent` / `ExpenseFormComponent` e o padrão de overlay já usado em Gerenciar; `POST /api/income` e `POST /api/expense` inalterados.

## Technical Context

**Language/Version**: TypeScript (Angular 17) + Java 17 (Spring Boot 3.3.4) — backend sem mudança de contrato

**Primary Dependencies**:
- Frontend: Angular 17 standalone, Reactive Forms, overlays CSS existentes (`DayEntriesDialogComponent`)
- Backend: endpoints atuais de income/expense/history/balance (JWT)

**Storage**: MySQL 8.x — **sem** migração. Lançamentos nas tabelas já usadas em 001.

**Testing**: Validação manual via [quickstart.md](./quickstart.md) (spec 004 não pede suíte automatizada)

**Target Platform**: Navegador desktop (mesmo recorte da 001–003)

**Project Type**: Web application — Angular SPA + Spring Boot REST API

**Performance Goals**: Após salvar, modal fechado e linha da data visível em até 2 s (SC-004); registro a partir da linha em menos de 1 min sem redigitar a data (SC-003)

**Constraints**: Reusar campos/validações atuais; um modal de registro por vez; overlay click não fecha; Escape e Fechar fecham; Gerenciar coexiste na linha; sem alterar relatórios/perfil/configurações

**Scale/Scope**: Uma tela (`/history`); duas origens de abertura (topo vs linha); recorte mensal já existente

## Constitution Check

*GATE: Constituição do projeto (`constitution.md`) contém apenas o template de exemplo sem princípios definidos — nenhum gate formal a verificar.*

**Pós-design**: Sem violação. Só UI no histórico; contratos HTTP e schema inalterados.

## Project Structure

### Documentation (this feature)

```text
specs/004-history-entry-modals/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── ui-contract.md
│   └── api-contract.md
└── tasks.md             # /speckit-tasks — ainda não criado
```

### Source Code (repository root)

```text
frontend/src/app/
├── history/
│   ├── history.component.ts      # estado do modal; mês após save; atalhos na linha
│   ├── history.component.html    # overlay no lugar dos cards; botões por row
│   └── day-entries-dialog.component.ts  # Gerenciar — inalterado
├── income/income-form/           # @Input initialDate; @Output saved(date)
└── expense/expense-form/         # idem

backend/                             # sem alteração nesta feature
```

**Structure Decision**: Continua web app já existente. Overlay alinhado aos diálogos do histórico; sem módulo de domínio novo e sem Flyway.

## Complexity Tracking

Nenhuma violação de constituição.

| Decisão | Alternativa Rejeitada | Motivo |
|---------|----------------------|--------|
| Overlay CSS igual a Gerenciar | MatDialog / CDK | App já padroniza overlay; backdrop do Material fecha por padrão (contra FR-007) |
| Recriar o form no `*ngIf` | Patch em instância viva | Evita vazamento de campos entre aberturas |
| Emitir a data no `saved` e mudar `month` | Ficar no mês atual | Clarificação Q2 / FR-006 |
