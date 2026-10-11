# Implementation Plan: Sessão obrigatória e troca de senha esquecida

**Branch**: `003-session-password-reset` | **Date**: 2026-10-11 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `specs/003-session-password-reset/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Toda tela autenticada (incluindo endereço inexistente) exige sessão aberta; caso contrário o usuário vai para `/login`. Na tela de login, **Trocar senha** redefine a senha de uma conta existente só com e-mail + senha nova confirmada (sem senha atual). Não abre sessão; a pessoa entra de novo. Tokens JWT já emitidos para aquela conta deixam de valer na hora.

Abordagem: reforçar o `authGuard` e o catch-all do Angular; endpoint público `POST /api/auth/forgot-password`; coluna `token_version` no usuário, claim no JWT e checagem no filtro.

## Technical Context

**Language/Version**: Java 17 (Spring Boot 3.3.4) + TypeScript (Angular 17)

**Primary Dependencies**:
- Backend: Spring Security (JWT já existente), Bean Validation, Flyway (V10 — `token_version`)
- Frontend: Angular 17 standalone, `authGuard`, interceptor JWT (401 → login)

**Storage**: MySQL 8.x — coluna nova `users.token_version` (INT, default 1). Sem tabela de sessões.

**Testing**: Validação manual via [quickstart.md](./quickstart.md) (spec 003 não pede suíte automatizada)

**Target Platform**: Navegador desktop (mesmo recorte da 001/002)

**Project Type**: Web application — Angular SPA + Spring Boot REST API

**Performance Goals**: Redirecionamento ao login em até 2 s (SC-001/SC-006); troca de senha esquecida em menos de 2 min (SC-003)

**Constraints**: Isolamento por e-mail único; não enumerar contas (resposta genérica); não enviar e-mail; não alterar `PUT /users/me/password`; confirmação de senha obrigatória no fluxo esquecido

**Scale/Scope**: Um usuário por conta; JWT atual ~24 h; invalidação imediata via versão, não via blacklist

## Constitution Check

*GATE: Constituição do projeto (`constitution.md`) contém apenas o template de exemplo sem princípios definidos — nenhum gate formal a verificar.*

**Pós-design**: Sem violação. Reusa `users`, JWT e telas de auth; acrescenta um endpoint público, uma coluna e ajustes de rota.

## Project Structure

### Documentation (this feature)

```text
specs/003-session-password-reset/
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
├── auth/                    # forgot-password; JWT com token_version
├── config/                  # filtro rejeita versão antiga; /api/auth/** público
└── user/                    # coluna token_version; incremento na troca esquecida

backend/src/main/resources/db/migration/
└── V10__user_token_version.sql

frontend/src/app/
├── app.routes.ts            # guard em autenticadas; ** sem sessão → /login
├── auth/auth.guard.ts       # sessão local; guest para /login se já autenticado
├── auth/login/              # botão Trocar senha; e-mail + senha + confirmação
└── shared/interceptors/     # 401 continua limpando token e indo a /login
```

**Structure Decision**: Continua Option 2 (web app já existente). Sem novo módulo de domínio.

## Complexity Tracking

Nenhuma violação de constituição.

| Decisão | Alternativa Rejeitada | Motivo |
|---------|----------------------|--------|
| `token_version` no usuário + claim JWT | Blacklist de tokens | Tabela extra e limpeza de expirados sem ganho |
| Resposta 204 genérica no forgot | 404 se e-mail não existe | Enumeraria contas (FR-008) |
| Confirmação no cliente e no body | Só no Angular | API direta poderia gravar senha sem repetir |
