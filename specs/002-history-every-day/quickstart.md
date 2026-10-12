# Quickstart: Histórico dia a dia e exclusão em lote

**Feature**: 002-history-every-day | **Date**: 2026-10-05

Pré-requisitos: API no ar (`mvn spring-boot:run` com `DB_USER`/`DB_PASSWORD`), usuário autenticado. Contrato: [`contracts/api-contract.md`](./contracts/api-contract.md).

Token: `POST /api/auth/login` (ou register). Header `Authorization: Bearer {token}` nas chamadas abaixo.

---

## Cenário A — Calendário contínuo (US1, US2, US3)

**Setup**: período 2026-10-01 a 2026-10-10. Criar só:

- `POST /api/income` `{ "date": "2026-10-03", "amount": 1000.00, "description": "A" }`
- `POST /api/expense` `{ "date": "2026-10-07", "amount": 200.00, "description": "B" }`

```http
GET /api/history?from=2026-10-01&to=2026-10-10
```

**Esperado**:
- `rows.length === 10`
- Datas 01…10 sem buraco
- 03/10: `daily_income = 1000`, `running_balance = 1000`
- 04/10, 05/10, 06/10: `entries = []`, `running_balance = 1000`
- 07/10: `daily_expense_confirmed = 200`, `running_balance = 800`
- 08/10–10/10: vazios, `running_balance = 800`

Na UI: linhas vazias visualmente distintas; lançamentos do dia 03 e 07 visíveis **sem** botão excluir por item.

---

## Cenário B — Exclusão parcial no diálogo (US4)

Continuando do Cenário A, no dia 07 (ou via API equivalente ao fluxo da UI):

1. Abrir gerenciar do dia 07 — lista contém a saída B (selecionável).
2. Marcar B → Apagar → **tem certeza?** → confirmar.
3. Sem recorrente: não abre diálogo de escopo.

Equivalente API:

```http
POST /api/history/day-deletions
{ "date": "2026-10-07", "items": [ { "id": {id_B}, "type": "EXPENSE" } ] }
```

**Esperado**: 204. `GET /history` no mesmo intervalo: dia 07 vazio, `running_balance` 07–10 = 1000. Diálogo fecha (lista vazia).

---

## Cenário C — Apagar sem seleção e recusar confirmação

1. Abrir diálogo de um dia com itens, **não** marcar nada, clicar Apagar → mensagem; nada some; sem “tem certeza?”.
2. Marcar um item, Apagar, **recusar** “tem certeza?” → item permanece; diálogo de lista segue aberto.

---

## Cenário D — Recorrentes no mesmo clique (FR-019)

1. Criar saída recorrente mensal em 2026-10-05 (aluguel 1200) e outra não recorrente no mesmo dia (50).
2. Abrir 05/10, marcar as duas, Apagar, confirmar “tem certeza?”.
3. Um único diálogo de escopo → `THIS_ONLY`.

```http
POST /api/history/day-deletions
{
  "date": "2026-10-05",
  "items": [
    { "id": {id_aluguel}, "type": "EXPENSE" },
    { "id": {id_50}, "type": "EXPENSE" }
  ],
  "recurrence_scope": "THIS_ONLY"
}
```

**Esperado**: 05/10 sem os dois itens; 05/11 e 05/12 ainda mostram aluguel 1200 (projeção). Se ainda houver outros itens no dia, o diálogo permanece aberto.

---

## Cenário E — Fatura não apagável (FR-018)

Com cartão e compra pendente, o dia de vencimento mostra `CREDIT_CARD_BILL`. O diálogo de exclusão **não** lista essa fatura. Enviar esse tipo no POST → 422.

---

## Fora de escopo deste guia

Relatório e CSV continuam sem linhas de dias vazios (FR-010).
