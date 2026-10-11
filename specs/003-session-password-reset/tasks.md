# Tasks: Sessão obrigatória e troca de senha esquecida

**Input**: Design documents from `specs/003-session-password-reset/`

**Prerequisites**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/api-contract.md](./contracts/api-contract.md) · [quickstart.md](./quickstart.md)

**Stack**: Java 17 + Spring Boot 3.3.4 · Angular 17 · MySQL 8.x (Flyway V10 — `token_version`)

**Tests**: não solicitados na spec — omitidos

**Total tasks**: 16

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: paralelo (arquivos diferentes, sem dependência incompleta)
- **[US#]**: user story da spec (US1–US2)
- Sem label = Setup, Foundational ou Polish

---

## Phase 1: Setup

**Purpose**: Confirmar que a feature reusa o app 001/002 e acrescenta só V10 + auth.

- [X] T001 Confirmar Flyway V10 em `backend/src/main/resources/db/migration/V10__user_token_version.sql` e que login/register continuam em `backend/src/main/java/com/financia/auth/AuthController.java` (público `/api/auth/**`)

---

## Phase 2: Foundational (bloqueia as stories)

**Purpose**: Todo JWT carrega `token_version`; o filtro rejeita versão divergente. Login existente continua válido após o deploy (claim ausente = 1).

**⚠️ CRÍTICO**: Sem isso, a troca esquecida não consegue invalidar sessões (FR-011) e tokens novos quebram o login.

- [X] T002 Criar migração `users.token_version` INT NOT NULL DEFAULT 1 — `backend/src/main/resources/db/migration/V10__user_token_version.sql`
- [X] T003 Campo `tokenVersion` (`token_version`, int, NOT NULL, default 1) em `backend/src/main/java/com/financia/user/User.java`
- [X] T004 Incluir claim `token_version` em `generateToken` (valor atual do usuário no login); claim ausente no parse trata-se como 1 — `backend/src/main/java/com/financia/auth/JwtTokenProvider.java`
- [X] T005 Em `JwtAuthenticationFilter`, se `token_version` do JWT ≠ `User.tokenVersion`, **não** autenticar (pedido protegido → 401) — `backend/src/main/java/com/financia/config/JwtAuthenticationFilter.java`

**Checkpoint**: Login atual emite JWT com versão 1; `GET /api/users/me` com token antigo sem claim ainda funciona.

---

## Phase 3: User Story 1 — Área autenticada só com sessão aberta (P1) 🎯 MVP

**Goal**: Histórico, relatórios, configurações, perfil, raiz e URL inexistente exigem sessão; senão `/login`. Com sessão, `/login` vai ao histórico. 401 limpa token.

**Independent Test**: Quickstart Cenário A (janela anônima + depois logado).

- [X] T006 [US1] Manter `canActivate: [authGuard]` no layout autenticado (`history`, `reports`, `settings`, `profile`) — `frontend/src/app/app.routes.ts`
- [X] T007 [US1] Catch-all `**`: sem token local → `/login`; com token → `/history` (não mandar anônimo ao histórico) — `frontend/src/app/app.routes.ts`
- [X] T008 [US1] Guard inverso em `/login`: se `isAuthenticated()`, redirecionar ao histórico — `frontend/src/app/auth/auth.guard.ts` (ou `guest.guard.ts`) e `frontend/src/app/app.routes.ts`
- [X] T009 [P] [US1] Confirmar que 401 fora de `/auth/` chama `logout()` e `navigateByUrl('/login')` — `frontend/src/app/shared/interceptors/jwt.interceptor.ts`

**Checkpoint**: Quickstart A na UI (anônimo não vê dados; logado navega; `/login` com sessão → histórico).

---

## Phase 4: User Story 2 — Trocar senha quando a pessoa não se lembra da atual (P1)

**Goal**: Botão **Trocar senha** no login; e-mail + senha nova + confirmação; 204 genérico; sem JWT; incrementa `token_version`; senha antiga falha; sessões antigas 401.

**Independent Test**: Quickstart B, C e D.

- [X] T010 [P] [US2] DTO `ForgotPasswordRequest` com `email` obrigatório (formato e-mail), `newPassword` obrigatório `@Size(min = 6)`, `newPasswordConfirmation` obrigatório e igual a `newPassword` — `backend/src/main/java/com/financia/auth/dto/ForgotPasswordRequest.java`
- [X] T011 [US2] `AuthService.forgotPassword`: validação 422 se senhas diferirem ou abaixo do mínimo; se e-mail existir (mesmo critério case-insensitive do login), atualizar `password_hash` e `token_version += 1`; se não existir, não gravar; **nunca** emitir JWT; e-mail existente ou não → mesmo sucesso — `backend/src/main/java/com/financia/auth/AuthService.java`
- [X] T012 [US2] `POST /api/auth/forgot-password` 204 em `AuthController` (permitAll já cobre `/api/auth/**`) — `backend/src/main/java/com/financia/auth/AuthController.java`
- [X] T013 [P] [US2] `forgotPassword(email, newPassword, confirmation)` → `POST .../auth/forgot-password` **sem** gravar token — `frontend/src/app/auth/auth.service.ts`
- [X] T014 [US2] Modo `forgot` na tela de login: botão **Trocar senha**; campos e-mail, senha nova, confirmação; divergência → erro local sem chamar API; 204 → modo login + mensagem genérica “Se houver conta com este e-mail, a senha foi atualizada. Entre com a senha nova.”; **não** chamar `login` automaticamente — `frontend/src/app/auth/login/login.component.ts` e `login.component.html`

**Checkpoint**: Quickstart B–D (confirmação, 204 genérico, senha nova no login, JWT antigo 401).

---

## Phase 5: Polish

**Purpose**: Consistência e validação.

- [X] T015 Executar cenários A–D de [quickstart.md](./quickstart.md) (API + UI) e registrar o resultado neste `tasks.md`

  **Resultado (2026-10-11):** API B/C/D OK (422 confirmação, 204 genérico, senha nova no login, JWT antigo **401**). UI A: `/history`, `/reports` e `/nao-existe` sem sessão → `/login`; logado em `/login` → histórico. UI B: **Trocar senha**, divergência mostra erro local; 204 volta ao login com mensagem genérica e **sem** token.
- [X] T016 [P] Garantir que `PUT /users/me/password` **não** incrementa `token_version` — `backend/src/main/java/com/financia/user/UserService.java`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup**: imediato
- **Foundational (T002–T005)**: bloqueia US1 (login continua) e US2 (invalidação)
- **US1**: depois da Foundational — MVP de rotas
- **US2**: depois da Foundational; independente da UI da US1, mas o 401 da US1 reforça FR-011
- **Polish**: depois das stories

### User Story Dependencies

- **US1 (P1)**: só Foundational
- **US2 (P1)**: Foundational; T010 antes de T011; T011 antes de T012; T013 [P] com T010; T014 depois de T013

### Parallel Opportunities

- T009 em paralelo com T008 (interceptor vs guard) depois de T006–T007 nas rotas
- T010 e T013 em paralelo
- T016 em paralelo com T015 no polish

---

## Parallel Example: User Story 2

```text
Task: "ForgotPasswordRequest em backend/.../auth/dto/ForgotPasswordRequest.java"
Task: "forgotPassword em frontend/.../auth/auth.service.ts"
```

---

## Implementation Strategy

### MVP (US1)

1. Setup T001  
2. Foundational T002–T005  
3. US1 T006–T009  
4. Validar Cenário A e parar se for demo só do bloqueio

### Incremental

1. US1 → ninguém anônimo vê dados  
2. US2 → Trocar senha + invalidar JWT  
3. Polish / quickstart A–D

### Suggested MVP scope

**T001–T009** (sessão nas rotas). Troca esquecida (US2) no segundo incremento.

---

## Independent test criteria (resumo)

| Story | Como testar sozinha |
|-------|---------------------|
| US1 | Cenário A: URLs autenticadas e inexistentes → `/login`; com sessão, navegação ok |
| US2 | Cenários B–D: confirmação, 204 genérico, senha nova, JWT antigo 401 |

---

## Notes

- Formato checklist: checkbox + T00x + [P]? + [US#]? + descrição com path
- Sem tarefas de teste automatizado (não pedidas)
- `PUT /users/me/password` permanece; não incrementa `token_version` nesta feature
