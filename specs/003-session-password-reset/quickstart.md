# Quickstart: Sessão obrigatória e troca de senha esquecida

**Feature**: 003-session-password-reset | **Date**: 2026-10-11

Pré-requisitos: API no ar, app Angular no ar. Contrato: [`contracts/api-contract.md`](./contracts/api-contract.md).

---

## Cenário A — Sem sessão não entra (US1)

**Setup**: janela anônima (sem token).

1. Abrir `/history`, `/reports`, `/settings`, `/profile` e um endereço inexistente (ex. `/nao-existe`).
2. **Esperado**: em todos, URL `/login` em até 2 s; nenhum saldo, lançamento ou relatório.

3. Entrar com e-mail/senha válidos.
4. **Esperado**: histórico abre; navegar entre as telas autenticadas **não** volta ao login.

5. Com sessão, abrir `/login`.
6. **Esperado**: vai ao histórico (não parece deslogado).

---

## Cenário B — Trocar senha esquecida (US2)

**Setup**: conta existente (registrar se preciso). Anotar a senha antiga. Estar na tela de login **sem** sessão.

1. Clicar **Trocar senha**.
2. Preencher e-mail da conta, senha nova e confirmação **diferente** → **Esperado**: erro; senha antiga ainda autentica em `POST /auth/login`.
3. Confirmação igual à senha nova (≥ 6 caracteres) → **Esperado**: volta ao formulário de login, **sem** entrar na área autenticada.
4. `POST /auth/login` com senha antiga → **401**.
5. `POST /auth/login` com senha nova → **200** e o histórico abre.

API equivalente:

```http
POST /api/auth/forgot-password
{ "email": "{email}", "new_password": "novaSenha123", "new_password_confirmation": "novaSenha123" }
```

**204**. Sem header `Authorization` na resposta.

---

## Cenário C — E-mail inexistente (FR-008)

```http
POST /api/auth/forgot-password
{ "email": "ninguem@naoexiste.local", "new_password": "abcdef", "new_password_confirmation": "abcdef" }
```

**Esperado**: **204** (igual ao sucesso). UI: mensagem genérica, sem “e-mail não cadastrado”.

---

## Cenário D — Sessões antigas caem (FR-011)

1. Login na conta (guardar o JWT).
2. Em outro cliente (ou via API), `POST /auth/forgot-password` com senha nova válida.
3. Com o JWT **antigo**, `GET /api/history?from=2026-10-01&to=2026-10-01`.

**Esperado**: **401**. Na UI com esse token, a próxima ação autenticada vai a `/login` em até 2 s, sem dados financeiros.
