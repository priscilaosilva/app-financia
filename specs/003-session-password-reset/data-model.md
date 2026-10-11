# Data Model: Sessão obrigatória e troca de senha esquecida

**Feature**: 003-session-password-reset | **Date**: 2026-10-11

Reusa `users` de `001-finance-management`. Uma coluna nova. Sem tabela de sessões.

## Entidades persistidas

### Usuário (`users`)

| Campo | Tipo | Regras |
|-------|------|--------|
| id | long | PK existente |
| email | string | Único; identificador da troca esquecida (case-insensitive como no login atual) |
| password_hash | string | bcrypt; na troca esquecida vira o hash da senha nova |
| token_version | int | **Novo.** NOT NULL, default 1. Incrementa em +1 quando a troca esquecida é aceita |
| name, theme_preference, timestamps | — | Inalterados |

**Invariantes**:
- `token_version` só aumenta (nunca volta a 0)
- Tokens JWT com claim `token_version` diferente do banco são sessão inválida
- Perfil (`PUT /users/me/password`) não altera `token_version` nesta feature

### Sessão (não persistida)

Representada pelo JWT no cliente.

| Claim | Origem |
|-------|--------|
| sub | e-mail |
| userId | id |
| token_version | `users.token_version` no momento do login |
| iat / exp | iguais aos tokens atuais |

**Transições**:
- Login com senha correta → emite JWT com a versão atual
- Forgot-password aceito → `password_hash` novo, `token_version += 1` → JWTs antigos inválidos
- Logout / 401 / ausência de token → sem sessão; destino `/login`

## Entidade de comando (não persistida)

### Troca de senha esquecida

| Campo | Tipo | Regras |
|-------|------|--------|
| email | string | Obrigatório; formato de e-mail |
| new_password | string | Obrigatório; mesmo mínimo do cadastro (≥ 6) |
| new_password_confirmation | string | Obrigatório; deve ser igual a `new_password` |

**Resultado**:
- Validação falha → nada gravado, sessões intactas
- E-mail inexistente → nada gravado, 204 genérico
- E-mail existente e válido → hash + versão; nenhuma sessão nova
