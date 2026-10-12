# Quickstart: Modais de nova entrada e nova saída no histórico

**Feature**: 004-history-entry-modals | **Date**: 2026-10-11

Pré-requisitos: API e Angular no ar, sessão válida (003). Contratos: [`contracts/ui-contract.md`](./contracts/ui-contract.md), [`contracts/api-contract.md`](./contracts/api-contract.md). Modelo de estado: [`data-model.md`](./data-model.md).

---

## Cenário A — Topo, data em branco (US1)

1. Abrir `/history` autenticado.
2. Clicar **Nova entrada**.
3. **Esperado**: overlay sobre a planilha (tabela visível ao fundo); campo data vazio; a tabela **não** desceu para abrir um card.
4. Clicar no fundo (histórico). **Esperado**: modal continua aberto.
5. Escape ou Fechar/Cancelar. **Esperado**: modal some; nenhum lançamento novo; mesmo mês.
6. Abrir de novo, preencher data + valor (e demais obrigatórios) e salvar.
7. **Esperado**: modal fecha; a linha dessa data mostra a entrada; saldo do topo atualiza.

Repetir 2–7 com **Nova saída**.

---

## Cenário B — Atalho da linha, data preenchida (US2)

1. No mês visível, numa linha **com** movimento e numa **vazia**, conferir atalhos compactos de entrada e saída **e** (se houver o que apagar) **Gerenciar**.
2. Na linha 07 do mês (ou qualquer dia visível), atalho de entrada.
3. **Esperado**: modal de entrada; data = a da linha (`YYYY-MM-DD` no input).
4. Salvar só com valor (sem mudar a data).
5. **Esperado**: modal fecha; o lançamento está nessa mesma linha.

Repetir atalho de saída numa linha vazia.

---

## Cenário C — Data de outro mês (FR-006)

1. No mês atual, **Nova entrada** no topo.
2. Escolher uma data do **mês seguinte**, valor válido, salvar.
3. **Esperado**: em até 2 s o rótulo do mês (setas) é o mês dessa data; a planilha lista esse mês; a linha da data tem o lançamento.

---

## Cenário D — Um modal e validação (FR-005, FR-008)

1. Abrir **Nova entrada**. Sem fechar, clicar **Nova saída**.
2. **Esperado**: só o modal de saída; data em branco (origem topo).
3. Tentar salvar sem data. **Esperado**: não POST; modal aberto; data continua vazia.
