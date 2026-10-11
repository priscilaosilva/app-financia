# Research: Histórico dia a dia e exclusão em lote

**Feature**: 002-history-every-day | **Date**: 2026-10-05

## 1. Onde preencher os dias vazios

**Decision**: Preencher o calendário no backend, em `HistoryService.history`, depois de calcular lançamentos e saldo de abertura.

**Rationale**: Um único contrato (`GET /history`) garante que qualquer cliente vê N linhas para N dias. O saldo de abertura já existe (`openingBalance`); basta caminhar dia a dia de `from` a `to`, reusando o `running` mesmo quando `entries` está vazio.

**Alternatives considered**:
- Preencher no Angular a partir das rows esparsas — risco de saldo inconsistente se outro cliente consumir a API
- Persistência de dias — rejeitado pela spec (dias vazios não são registros)

## 2. Como marcar dia vazio na UI

**Decision**: Dia vazio = `entries` vazio (sem INCOME/EXPENSE/CREDIT_CARD_BILL). CSS na linha (ex. classe `empty-day`, tipografia mais clara). Não precisa de campo extra, mas `has_movement: boolean` no DTO é opcional para testes.

**Rationale**: SC-003 pede distinção em menos de 5 segundos; cor/opacidade na linha basta. Evita duplicar lógica “o que conta como movimento”.

**Alternatives considered**: Ícone “sem movimento”, ocultar colunas — mais ruído na planilha.

## 3. Contrato de exclusão em lote

**Decision**: `POST /api/history/day-deletions` com `{ "date", "items": [{ "id", "type" }], "recurrence_scope"? }`.

**Rationale**:
- Todos os itens do modal compartilham o mesmo dia (`date`)
- Um `recurrence_scope` para todos os recorrentes/projeções do clique (FR-019)
- Transação única: falha em um item desfaz o lote
- Fatura `CREDIT_CARD_BILL` (`id` nulo) → 422 se enviada

Para projeções, `id` é o do lançamento-base; `date` do body é a ocorrência a cancelar/modificar (`THIS_ONLY` grava exceção naquela data, não na data-base). Isso corrige o limite atual de `DELETE /expense/{id}` que usa só a data do registro persistido.

**Alternatives considered**:
- Loop de `DELETE /income/{id}` e `DELETE /expense/{id}` no frontend — N round-trips e escopo por item
- Novo recurso REST por item — não atende “um Apagar”

Deletes avulsos `DELETE /income/{id}` e `DELETE /expense/{id}` permanecem para outros fluxos; a planilha deixa de usá-los.

## 4. Fluxo de diálogos no frontend

**Decision**:
1. Ação na linha do dia (botão “Gerenciar” / ícone) só se existir ao menos um item com `id` e `type` INCOME|EXPENSE
2. Diálogo-lista com checkboxes
3. “Apagar” sem seleção → mensagem, sem confirmação
4. “Apagar” com seleção → diálogo “tem certeza?”
5. Recusa → volta à lista, seleção intacta
6. Aceite + algum item recorrente ou `is_recurring_projection` → um `RecurrenceScopeDialog`
7. `POST /history/day-deletions` → recarrega `GET /history` (e saldo do topo)
8. Se o dia ainda tiver itens apagáveis, o diálogo-lista permanece com a lista nova; senão fecha

**Rationale**: Espelha FR-016, FR-016a, FR-017, FR-019. Reusa o componente de escopo já existente.

**Alternatives considered**: `window.confirm` apenas — aceitável para o “tem certeza?”, mas Material/componente interno fica mais consistente.

## 5. Relatórios

**Decision**: Não alterar `ReportService` / CSV.

**Rationale**: FR-010 — preenchimento só na visão de histórico.

## 6. Performance

**Decision**: Loop O(dias do período) em memória; mês (~31) e 90 dias são triviais. Sem cache.

**Rationale**: Volume continua pequeno; o custo extra é gerar linhas vazias, não consultar o banco a mais.

**Alternatives considered**: Paginação por semana — a spec pede sequência contínua no período visível (mês com setas).
