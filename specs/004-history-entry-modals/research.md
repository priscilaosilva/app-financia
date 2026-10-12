# Research: Modais de nova entrada e nova saída no histórico

**Feature**: 004-history-entry-modals | **Date**: 2026-10-11

## 1. Invólucro do formulário (modal vs bloco na página)

**Decision**: Trocar os cards inline (`*ngIf="showIncome"` / `showExpense` acima da tabela) por um overlay `position: fixed` no mesmo padrão de `DayEntriesDialogComponent` / `ConfirmDeleteDialogComponent`. O conteúdo do overlay é o `app-income-form` ou `app-expense-form` já existente. Um único estado no histórico: `entryModal: 'income' | 'expense' | null` (nunca os dois).

**Rationale**: FR-001 e FR-008. O overlay já usado no histórico cobre a planilha sem empurrá-la. Material `MatDialog` está no `package.json` mas nenhum diálogo do app o usa; misturar CDK Dialog só para este fluxo aumentaria superfície sem ganho. O formulário atual já POST em `/api/income` e `/api/expense` com as validações da spec (FR-005).

**Alternatives considered**:
- `MatDialog` / CDK Dialog — fecha por padrão no backdrop; exigiria `disableClose` + Escape custom; diverge dos overlays atuais
- Manter o card e só “fixar” com CSS sticky — ainda empurra ou compete com o scroll da planilha
- Duplicar os campos num componente novo — viola FR-005 (reusar registro atual)

## 2. Clique no fundo vs Escape / Fechar

**Decision**: O overlay **não** tem `(click)` para fechar. O painel interno usa `$event.stopPropagation()` por consistência, mas o fechamento só vem de: botão Fechar/Cancelar, tecla Escape (`document:keydown.escape` enquanto o modal está aberto), ou sucesso no `saved`. Clique no backdrop não descarta o formulário (FR-007).

**Rationale**: Clarificação Q1. Os diálogos atuais de gerenciar/apagar também não fecham no fundo; o registro segue a mesma regra, agora explícita.

**Alternatives considered**: Fechar no overlay — rejeitado na clarificação. `disableClose` do Material — não se aplica ao overlay próprio.

## 3. Data inicial (topo vs linha)

**Decision**: Os formulários ganham `@Input() initialDate: string | null`. O histórico abre o modal com `initialDate = ''` (topo) ou `initialDate = row.date` (atalho da linha, ISO `YYYY-MM-DD` já usado na planilha e no `<input type="date">`). Cada abertura usa `*ngIf` no overlay para recriar o formulário (campos limpos, data só a da origem). A pessoa pode editar a data depois; o POST usa o valor do campo, não o da linha original.

**Rationale**: FR-002, FR-004. Recriar o componente evita residual do último preenchimento (edge: falha ao salvar mantém a instância aberta; só recria ao fechar e reabrir).

**Alternatives considered**: `patchValue` sem destruir o form — risco de vazar valor/descrição da abertura anterior. Query string na rota — overkill para um overlay na mesma tela.

## 4. Depois de salvar: ir ao período da data

**Decision**: `saved` passa a emitir a `date` salva (`YYYY-MM-DD`). O histórico fecha o modal, ajusta `this.month` para o 1º dia daquele mês (mesmo recorte das setas ←/→), chama `reload()` (GET `/history?from=&to=` do mês + GET `/balance`). Se a data já estava no mês visível, só recarrega. Destaque: a linha já existe no calendário contínuo (002); opcionalmente `scrollIntoView` na `tr` da data.

**Rationale**: Clarificação Q2 / FR-006 / SC-004. Não há endpoint novo: o GET de histórico por intervalo já cobre qualquer mês.

**Alternatives considered**: Permanecer no mês atual e só avisar — rejeitado na clarificação. Recarregar o ano inteiro — mais payload sem necessidade.

## 5. Atalhos compactos na linha

**Decision**: Na última coluna da linha (junto de **Gerenciar**), dois botões `btn ghost` compactos (rótulos curtos, p.ex. **+E** / **+S** ou **Entrada** / **Saída** em tamanho reduzido), visíveis em **toda** linha do período, inclusive `empty-day`. **Gerenciar** continua só se `hasDeletable(row)`. Os atalhos não substituem Gerenciar (FR-009).

**Rationale**: FR-003. A coluna de ações já existe; não precisa de coluna nova. Dias vazios já são linhas (`has_movement === false`).

**Alternatives considered**: Ícones só — menos óbvio sem aria-label. Menu único “…” — esconde o atalho de um clique (SC-003).

## 6. Backend e persistência

**Decision**: Nenhuma migração, entidade ou rota nova. Continua `POST /api/income` e `POST /api/expense` (201, JWT, regras 001) e `GET /api/history` + `GET /api/balance` após sucesso.

**Rationale**: Spec: “muda só o invólucro e a origem da data”.

**Alternatives considered**: Endpoint “create for date” — duplicaria validação.

## 7. Relatórios, perfil, configurações

**Decision**: Sem alteração. Formulários de entrada/saída só no histórico.

**Rationale**: FR-009.
