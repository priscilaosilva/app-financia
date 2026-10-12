# Research: Sessão obrigatória e troca de senha esquecida

**Feature**: 003-session-password-reset | **Date**: 2026-10-11

## 1. Como invalidar sessões JWT já emitidas

**Decision**: Coluna `users.token_version` (INT, NOT NULL, default 1). O JWT inclui claim `token_version`. No login, grava a versão atual. No `JwtAuthenticationFilter`, se a claim ≠ valor no banco, o token é ignorado (pedido autenticado responde 401). `POST /auth/forgot-password` bem-sucedido incrementa a versão.

**Rationale**: A API hoje é stateless (`SessionCreationPolicy.STATELESS`). Sem um dado persistido, um JWT válido até `expires_at` continuaria abrindo a área autenticada (quebra FR-011). Incrementar um inteiro invalida todos os tokens da conta sem lista negra.

**Alternatives considered**:
- Blacklist de `jti` — exige tabela, TTL e varredura
- Só esperar expirar — contradiz “na hora”
- Trocar o secret global — derrubaria **todas** as contas

`PUT /users/me/password` (perfil) **não** incrementa a versão nesta feature (FR-010).

## 2. Rotas Angular sem sessão

**Decision**:
- Layout autenticado (`history`, `reports`, `settings`, `profile`, raiz) continua com `canActivate: [authGuard]`
- Catch-all `**`: se não houver token local → `/login`; se houver → `/history` (evita mandar anônimo ao histórico antes do guard)
- `/login` ganha guard inverso: com sessão válida aparente, redireciona ao histórico (default da spec)
- Interceptor JWT já existente: 401 fora de `/auth/` → limpa token e `/login` (cobre versão inválida e expiração)

**Rationale**: Hoje `**` redireciona para `history`, o que só depois cai no guard. A spec pede que endereço inexistente **sem sessão** vá direto a `/login` e não “tela autenticada vazia”. Token local sem validade no servidor é tratado no primeiro 401.

**Alternatives considered**: Validar JWT no guard com `GET /users/me` em toda navegação — extra round-trip; o interceptor já cumpre SC-006 no primeiro pedido autenticado.

## 3. Contrato da troca esquecida

**Decision**: `POST /api/auth/forgot-password` (público, sob `/api/auth/**` já `permitAll`).

Body: `{ "email", "new_password", "new_password_confirmation" }`.

- Senha/confirmação divergentes ou senha abaixo do mínimo do cadastro → **422**, não altera hash nem versão
- E-mail existente + senhas iguais e válidas → atualiza hash, incrementa `token_version`, **204**, **sem** JWT
- E-mail inexistente → **204** com o mesmo vazio (mensagem genérica no cliente: “Se houver conta com este e-mail, a senha foi atualizada. Entre com a senha nova.”)
- Não exige senha atual, código nem token

**Rationale**: FR-007/008/009. 204 único impede enumeração. Confirmação no servidor impede cliente que ignore o segundo campo.

**Alternatives considered**:
- 404 para e-mail desconhecido — vaza existência da conta
- Auto-login (devolver JWT) — rejeitado na clarificação
- Reusar `PUT /users/me/password` — exige sessão e senha atual

## 4. UX na tela de login

**Decision**: Mesmo componente de login: modo extra `forgot` (como já existe `login` | `register`). Botão **Trocar senha** troca o modo. Campos: e-mail, senha nova, confirmação. Sucesso (204) volta ao modo login **sem** gravar token. Divergência de senhas: erro local (e 422 se chegar na API).

**Rationale**: FR-003/004: fluxo acessível sem sessão, no login, sem rota autenticada nova.

**Alternatives considered**: Rota `/trocar-senha` — mais uma pública para o catch-all tratar; desnecessário se o modo vive em `/login`.

## 5. Perfil e relatórios

**Decision**: Não alterar o fluxo autenticado de troca de senha no perfil nem relatórios/histórico além do guard.

**Rationale**: FR-010. Isolamento da feature.
