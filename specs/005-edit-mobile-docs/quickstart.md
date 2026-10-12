# Quickstart: Edição, gráfico diário, login, mobile e docs

**Feature**: 005-edit-mobile-docs | **Date**: 2026-10-11

Pré-requisitos: API e Angular no ar, sessão. Contratos: [`contracts/ui-contract.md`](./contracts/ui-contract.md), [`contracts/api-contract.md`](./contracts/api-contract.md).

---

## Cenário A — E-mail e Enter (US1)

1. `/login`, e-mail `nao-e-email`, senha `Senha@123`.
2. **Esperado**: **Entrar** bloqueado ou recusa na tela; Enter **não** chama login.
3. E-mail `voce@financia.com` (válido), senha mínima, Enter.
4. **Esperado**: tenta entrar (sucesso ou “credenciais inválidas”), igual ao botão.

Cadastro/forgot: o mesmo bloqueio de e-mail.

---

## Cenário B — Editar um item (US2)

1. Histórico: criar uma saída no dia 7; **Gerenciar**.
2. Sem marcar / marcar 2 itens (crie outro se preciso): **Editar** ausente.
3. Marcar só a saída → **Editar** → modal preenchido.
4. Mudar o valor, salvar (sem recorrência).
5. **Esperado**: edição some; Gerenciar ainda aberto com o valor novo; planilha atualizada.
6. Editar de novo, mudar tipo para **entrada**, salvar.
7. **Esperado**: na linha, entrada no lugar da saída.

Escape no modal de edição: lançamento inalterado; Gerenciar continua.

---

## Cenário C — Recorrência na edição (FR-006a)

1. Entrada com **Repetir** mensal; Gerenciar num dia da série; um item; Editar; alterar valor; Salvar.
2. **Esperado**: diálogo só este / a partir daqui / toda a série.
3. Cancelar o diálogo → não grava; modal de edição segue aberto.
4. Salvar de novo, escolher **só este** → só aquele dia muda.

---

## Cenário D — Gráfico (US3)

1. Relatórios, mês com movimento (e, se possível, recorrência futura).
2. **Esperado**: três linhas — saldo que continua no futuro; entradas e saídas **por dia** (pico no dia do lançamento, não rampa acumulada).
3. Período vazio → gráfico visível, não tela quebrada.

---

## Cenário E — iPhone 16 e 16 Pro Max (US4)

DevTools: 393×852 e 440×956, retrato. Login, histórico (Nova entrada + Gerenciar), relatórios.

**Esperado**: sem scroll horizontal da **página**; modais roláveis por dentro; ações da linha alcançáveis.

---

## Cenário F — Docs (US5)

1. `README.md` descreve o produto, não `mvn`/`npm` como conteúdo principal.
2. `app-settings.md` tem como subir API e front (comando a partir de `backend/`).
3. `RELEASE-NOTES.md` tem 001–005 em português.
