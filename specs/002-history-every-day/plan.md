# Implementation Plan: Histórico dia a dia e exclusão em lote

**Branch**: `002-history-every-day` | **Date**: 2026-10-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/002-history-every-day/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

O histórico passa a devolver **uma linha por dia civil** do intervalo `from`–`to`, com saldo carregado nos dias sem movimento. Na planilha, cada dia mostra totais e a lista de lançamentos, **sem** botão excluir por item. A exclusão acontece num diálogo do dia: seleção múltipla → “Apagar” → “tem certeza?” → (se houver recorrente) um único escopo → exclusão em lote. Faturas sintéticas de cartão não são apagáveis. Relatórios e CSV não mudam.

Abordagem: preencher o calendário no `HistoryService` (sem persistir dias vazios); novo endpoint de exclusão em lote no dia; Angular Material/diálogos existentes para lista, confirmação e escopo.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.3.4) + TypeScript (Angular 17)

**Primary Dependencies**:
- Backend: Spring Boot Web, Spring Data JPA, Bean Validation, Flyway (sem nova migração)
- Frontend: Angular 17 standalone, RxJS, diálogo de escopo já existente (`RecurrenceScopeDialogComponent`)

**Storage**: MySQL 8.x — sem tabelas novas; dias vazios são só de leitura

**Testing**: JUnit 5 (histórico contínuo + lote de exclusão); validação manual via [quickstart.md](./quickstart.md)

**Target Platform**: Navegador desktop (mesmo recorte da 001)

**Project Type**: Web application — Angular SPA + Spring Boot REST API

**Performance Goals**: Preencher e renderizar o mês visível (até ~31 linhas, ou 90 dias se o usuário ampliar o recorte) sem lentidão perceptível; `GET /history` continua on-demand

**Constraints**: Isolamento por usuário; não persistir linhas de dia vazio; não alterar contrato de relatórios/CSV; exclusão em lote transacional (tudo ou nada após confirmação no cliente)

**Scale/Scope**: Um usuário; período visível típico = 1 mês; horizonte de teste SC-005 = 90 dias contínuos

## Constitution Check

*GATE: Constituição do projeto (`constitution.md`) contém apenas o template de exemplo sem princípios definidos — nenhum gate formal a verificar.*

**Pós-design**: Sem violação adicional. A feature reutiliza o motor de saldo, as regras de recorrência e os endpoints de lançamento; só acrescenta preenchimento de calendário e um endpoint de lote.

## Project Structure

### Documentation (this feature)

```text
specs/002-history-every-day/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── api-contract.md
└── tasks.md             # /speckit-tasks — ainda não criado
```

### Source Code (repository root)

```text
backend/src/main/java/com/financia/
├── history/                 # preencher dias; DTO de linha; endpoint de lote
├── income/                  # delete reutilizado (escopo + data da ocorrência)
├── expense/
└── recurrence/              # THIS_ONLY na data da ocorrência selecionada

frontend/src/app/
├── history/                 # planilha contínua; modal de exclusão do dia
└── shared/components/       # confirmação + reuse do diálogo de escopo
```

**Structure Decision**: Continua Option 2 (web app já existente). Sem novos módulos de domínio.

## Complexity Tracking

Nenhuma violação de constituição.

| Decisão | Alternativa Rejeitada | Motivo |
|---------|----------------------|--------|
| Dias vazios só no read | Persistência de “placeholder days” | Inflaria o banco sem ganho |
| Endpoint de lote | N deletes no cliente | Um escopo para todos os recorrentes e transação única |
| Modal por dia | Lista do período inteiro | Spec: exclusão a partir da linha do dia |

## Phase 0: Research

Veja [`research.md`](./research.md).

## Phase 1: Design Artifacts

- [`data-model.md`](./data-model.md) — visão de dia (não persistida) + seleção de exclusão
- [`contracts/api-contract.md`](./contracts/api-contract.md) — `GET /history` contínuo + `POST /history/day-deletions`
- [`quickstart.md`](./quickstart.md) — cenários de calendário cheio e exclusão em lote
