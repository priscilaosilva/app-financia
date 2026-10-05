# Implementation Plan: Sistema de Gestão Financeira

**Branch**: `001-finance-management` | **Date**: 2026-10-04 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/001-finance-management/spec.md`

---

## Summary

Sistema de gestão financeira pessoal full-stack: Angular SPA + Spring Boot REST API + MySQL. O usuário registra entradas e saídas manualmente, visualiza histórico em formato de planilha com saldo acumulado dinâmico, gerencia transações recorrentes, controla cartões de crédito com período de faturamento e visualiza relatórios consolidados com exportação CSV. Saldo calculado on-demand via SQL; ocorrências recorrentes geradas em memória; gastos de cartão exibidos como "pendente" até o vencimento.

---

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.x) + TypeScript (Angular 17+)

**Primary Dependencies**:
- Backend: Spring Boot Web, Spring Data JPA, Spring Security, Bean Validation, JJWT (JWT auth), Lombok, MySQL Connector/J
- Frontend: Angular 17, RxJS, Angular Material (componentes UI), Chart.js ou ECharts (gráficos de projeção)

**Storage**: MySQL 8.x

**Testing**: JUnit 5 + Mockito (backend); Karma + Jasmine (frontend)

**Target Platform**: Navegador web desktop (Chrome, Firefox, Edge); mobile fora do escopo v1

**Project Type**: Web application — Angular SPA + Spring Boot REST API

**Performance Goals**:
- Histórico renderizado em < 2s para 5 anos de dados (~1.800 registros)
- Formulário de lançamento concluído pelo usuário em < 60s (SC-001)
- Relatório gerado em < 3s para qualquer período

**Constraints**:
- Isolamento estrito por usuário (dados de um usuário nunca visíveis a outro)
- Moeda única: BRL, sem conversão
- Sem integrações externas; todos os lançamentos são manuais
- Desktop-first; responsividade mobile não é requisito de v1

**Scale/Scope**: App single-user pessoal; sem requisitos de multi-tenancy em larga escala; ~1.800 registros por usuário para 5 anos

---

## Constitution Check

*GATE: Constituição do projeto (`constitution.md`) contém apenas o template de exemplo sem princípios definidos — nenhum gate formal a verificar neste momento.*

**Recomendação**: Antes de iniciar o desenvolvimento, preencher `.specify/memory/constitution.md` com os princípios do projeto (ex: convenções de nomenclatura, política de testes, padrões de segurança). Use `/speckit-constitution` para isso.

---

## Project Structure

### Documentation (this feature)

```text
specs/001-finance-management/
├── plan.md              ← Este arquivo (/speckit-plan)
├── research.md          ← Phase 0: decisões de arquitetura
├── data-model.md        ← Phase 1: modelo de dados completo
├── quickstart.md        ← Phase 1: guia de validação end-to-end
├── contracts/
│   └── api-contract.md  ← Phase 1: contrato REST API
└── tasks.md             ← Phase 2: gerado por /speckit-tasks
```

### Source Code (repository root)

```text
app-financia/                    ← raiz do repositório
│
├── backend/                     ← Spring Boot 3.x (Java 17)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/financia/
│   │   │   │   ├── auth/            ← registro, login, JWT filter
│   │   │   │   ├── user/            ← perfil, configurações, tema
│   │   │   │   ├── income/          ← entradas + fontes de receita
│   │   │   │   ├── expense/         ← saídas + categorias
│   │   │   │   ├── paymentmethod/   ← fontes de pagamento (cartão)
│   │   │   │   ├── recurrence/      ← regras e exceções de recorrência
│   │   │   │   ├── history/         ← motor de histórico + saldo
│   │   │   │   ├── report/          ← relatórios + exportação CSV
│   │   │   │   └── config/          ← Spring Security, CORS, JPA
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/    ← scripts SQL (Flyway ou Liquibase)
│   │   └── test/
│   │       └── java/com/financia/
│   │           ├── history/         ← testes do motor de saldo
│   │           ├── report/          ← testes de relatório
│   │           └── recurrence/      ← testes de geração de ocorrências
│   └── pom.xml
│
└── frontend/                    ← Angular 17 (TypeScript)
    ├── src/
    │   └── app/
    │       ├── auth/                ← login, registro, guards
    │       ├── layout/              ← shell com menu lateral
    │       ├── dashboard/           ← saldo total
    │       ├── history/             ← planilha de histórico + navegação
    │       ├── income/              ← formulário de entrada
    │       ├── expense/             ← formulário de saída
    │       ├── reports/             ← painel de relatórios + gráficos
    │       ├── settings/            ← configurações + cadastros auxiliares
    │       ├── profile/             ← perfil do usuário
    │       └── shared/              ← interceptors, services, models
    ├── angular.json
    └── package.json
```

**Structure Decision**: Option 2 (Web application) — backend e frontend em diretórios separados na raiz. A separação física permite deploys independentes e clareza de ownership.

---

## Phase 0: Research — Decisões Resolvidas

Veja [`research.md`](./research.md) para detalhes completos. Resumo:

| Decisão | Escolha | Justificativa |
|---------|---------|--------------|
| Cálculo de saldo | SQL dinâmico (`SUM`) a cada request | Volume pequeno (~1.800 registros); sem complexidade de invalidação |
| Recorrências | Virtual occurrence + `recurrence_exceptions` | Evita pré-gerar milhares de registros; exceções armazenadas por data |
| Cartão pendente | Campo `payment_status` + linha sintética no vencimento | Evita dados derivados persistidos; calculado no read |
| Projeções de relatório | Arrays `{date, cumulative}` via `GROUP BY DATE` | Frontend consome direto para gráficos; lógica centralizada no backend |

---

## Phase 1: Design Artifacts

### Data Model

Veja [`data-model.md`](./data-model.md) para o modelo completo.

**Tabelas principais**:
- `users` — autenticação e preferências (tema)
- `income_sources` — fontes de receita (soft delete via `active`)
- `expense_categories` — categorias de gasto (soft delete via `active`)
- `payment_methods` — contas e cartões de crédito (soft delete via `active`)
- `recurrence_rules` — regras de frequência (DAILY/MONTHLY/YEARLY)
- `recurrence_exceptions` — exceções por data (CANCELLED/MODIFIED)
- `income_entries` — lançamentos de entrada (com snapshot de nome da fonte)
- `expense_entries` — lançamentos de saída (com `payment_status` CONFIRMED/PENDING_CREDIT)

**Padrão de orphan (FR-022)**: exclusão de categorias/fontes usa soft delete (`active = FALSE`). Snapshots de nome nos lançamentos preservam o histórico.

### API Contract

Veja [`contracts/api-contract.md`](./contracts/api-contract.md) para o contrato REST completo.

**Grupos de endpoints**:
- `/auth/*` — registro e login (sem autenticação)
- `/users/me` — perfil, senha, tema, exclusão de conta
- `/balance` — saldo total atual
- `/history` — planilha de histórico com saldo acumulado por data
- `/income`, `/expense` — CRUD de lançamentos (com suporte a recorrência)
- `/income-sources`, `/categories`, `/payment-methods` — CRUD de cadastros auxiliares
- `/reports`, `/reports/export` — painel de relatório + exportação CSV

### Validation Guide

Veja [`quickstart.md`](./quickstart.md) para os 8 cenários de validação end-to-end cobrindo todos os FRs críticos.

---

## Complexity Tracking

Nenhuma violação de princípios de constituição identificada (constituição não preenchida). Decisões de design priorizaram simplicidade:

| Decisão | Alternativa Rejeitada | Motivo da Rejeição |
|---------|----------------------|-------------------|
| Saldo dinâmico por SQL | Running total armazenado | Sincronização frágil em lançamentos retroativos |
| Ocorrências virtuais | Pré-gerar todas as ocorrências | Geração excessiva de registros desnecessários |
| Linha de fatura sintética | Fatura persistida no banco | Dados derivados que podem sair de sincronia |
