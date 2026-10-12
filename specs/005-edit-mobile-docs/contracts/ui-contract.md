# UI contract: Login, Gerenciar/editar, gráfico, telefone, docs

**Feature**: 005-edit-mobile-docs | **Date**: 2026-10-11

## Login (`/login`)

| Regra | Contrato |
|-------|----------|
| E-mail | trim; inválido → botão de envio desabilitado + texto de erro no campo |
| Enter | no e-mail ou senha, com form válido, submete (login / cadastro / forgot conforme o modo) |
| Enter inválido | não chama API |

## Gerenciar

| Estado da seleção | Ações |
|-------------------|--------|
| 0 ou >1 | Apagar (se ≥1 e o fluxo atual exigir seleção) + Fechar; **Editar oculto** |
| exatamente 1 | **Editar** + Apagar + Fechar |

**Editar** abre o overlay de registro (z-index > Gerenciar), GET do item, `initialDate` e demais campos. Controle de tipo (entrada/saída) visível só neste fluxo.

Dispensa: Fechar/Escape/fundo — iguais à 004.

Salvar: ver [data-model.md](../data-model.md). Gerenciar permanece com lista do **mesmo** `date` recarregada; seleção vazia; fecha só se não houver apagáveis.

## Relatórios

Um canvas de linhas: **Saldo**, **Entradas**, **Saídas** (valores do dia nas duas últimas). KPI, categorias, CSV, filtros inalterados.

## Telefone (retrato)

Largura ≤ 480px: navegação no topo (não sidebar fixa larga); página sem overflow-x; tabela do histórico pode rolar **dentro** do card; overlay com scroll interno.

Verificação: ~393px (iPhone 16) e ~440px (iPhone 16 Pro Max).

## Docs (raiz)

Não são UI do app. Contratos de arquivo: [data-model.md](../data-model.md).
