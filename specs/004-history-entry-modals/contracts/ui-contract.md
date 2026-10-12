# UI contract: Modais de registro no histórico

**Feature**: 004-history-entry-modals | **Date**: 2026-10-11

Tela: `/history` (autenticada). Sem rotas novas.

## Superfície

### Topo (já existente, comportamento novo)

| Controle | Ação |
|----------|------|
| **Nova entrada** | Abre modal `kind=income`, `initialDate=''`. Fecha modal de saída se estiver aberto. Não altera o mês visível. |
| **Nova saída** | Abre modal `kind=expense`, `initialDate=''`. Fecha modal de entrada se estiver aberto. |

A planilha permanece visível ao fundo (escurecida pelo overlay). Não inserir card de formulário acima da tabela.

### Linha da planilha (toda `HistoryRow` do período)

| Controle | Visibilidade | Ação |
|----------|--------------|------|
| Atalho compacto entrada | Sempre | Modal `kind=income`, `initialDate=row.date` |
| Atalho compacto saída | Sempre | Modal `kind=expense`, `initialDate=row.date` |
| **Gerenciar** | Só se houver itens apagáveis | Inalterado (002). Não substitui os atalhos. |

## Modal

| Elemento | Contrato |
|----------|----------|
| Overlay | `position: fixed` cobrindo o viewport; planilha ao fundo; **clique no overlay não fecha** |
| Painel | Título “Registrar entrada” ou “Registrar saída”; formulário existente; **Fechar** ou **Cancelar** |
| Data | `<input type="date">`; vazia no topo; `YYYY-MM-DD` da linha no atalho |
| Escape | Fecha sem POST se o modal estiver aberto |
| Um de cada vez | Só `income` **ou** `expense` |

Campos internos (valor, fonte, categoria, pagamento, cartão pendente, descrição, repetir/frequência): ver formulários atuais; esta feature não remove nem adiciona regra de negócio.

## Depois de salvar (201)

1. Modal fecha (`kind=null`).
2. Mês visível = mês civil da `date` do POST (mesmo recorte `from`/`to` das setas).
3. Recarregar `GET /api/history` desse intervalo e `GET /api/balance`.
4. A linha `row.date === date` contém o novo lançamento (e totais atualizados) em até 2 s (SC-004).

Se a data já estava no mês atual: só passos 1, 3 e 4.

## Fora de escopo na UI

Relatórios, configurações, perfil, CSV. Diálogos Gerenciar / confirmar exclusão / escopo de recorrência não mudam o contrato, salvo coexistir na mesma linha.
