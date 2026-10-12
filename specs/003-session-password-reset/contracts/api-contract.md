# API Contract: Sessão obrigatória e troca de senha esquecida

**Feature**: 003-session-password-reset | **Base**: `/api` | **Auth**: Bearer JWT em todos os endpoints **exceto** `/auth/*`

Convenções 001: snake_case, erros `{ "error", "field?" }`.

---

## `POST /auth/forgot-password`

Redefine a senha de uma conta existente sem sessão e sem senha atual. **Não** devolve JWT.

**Request**

```json
{
  "email": "joao@email.com",
  "new_password": "novaSenha123",
  "new_password_confirmation": "novaSenha123"
}
```

**Response 204**: corpo vazio — tanto se o e-mail existir e a senha tiver sido atualizada, quanto se o e-mail **não** existir (FR-008).

**Errors**:
- **422** senha abaixo do mínimo, confirmação diferente, e-mail em formato inválido, campos ausentes — **não** altera hash nem `token_version`

Efeitos quando o e-mail existe e a validação passa:
- `password_hash` atualizado
- `token_version` incrementado (JWTs anteriores deixam de autenticar → **401** nos endpoints protegidos)

---

## Tokens existentes

`POST /auth/login` e `POST /auth/register` não mudam o envelope (`token`, `expires_at`). O JWT passa a carregar `token_version`. Clientes não precisam ler a claim; o servidor rejeita versão antiga.

`PUT /users/me/password` permanece como na 001 (exige sessão + senha atual).

---

## Falha de sessão (cliente)

Pedido autenticado com token ausente, expirado ou `token_version` divergente: **401**. O SPA limpa o token local e vai para `/login`.
