# Notas de versão — Financia

Registro em português das entregas Spec Kit. Cada implementação futura deve acrescentar uma seção aqui no polish.

## 001 — Gestão financeira

- Conta, saldo total e histórico em planilha (data, entrada, saída, saldo).
- Entradas e saídas com descrição, fonte ou categoria e forma de pagamento.
- Recorrência diária, mensal ou anual, com projeções nos meses futuros.
- Cartão de crédito: saída pendente até o ciclo; saldo considera o efeito no vencimento.
- Relatórios com totais, categorias, CSV e gráfico.
- Configurações (fontes, categorias, pagamentos) e perfil com tema.

## 002 — Todos os dias e exclusão em lote

- O histórico mostra **cada dia do calendário** do período, mesmo sem movimento.
- **Gerenciar** no dia: marcar um ou vários lançamentos e apagar.
- Recorrentes pedem o alcance (só esta, esta e futuras, todas) antes de apagar.

## 003 — Sessão e senha

- Rotas autenticadas exigem sessão; visitante não entra no histórico.
- Troca de senha por e-mail da conta, senha nova e confirmação.
- Tokens emitidos antes da troca deixam de valer (`token_version`).

## 004 — Modais de lançamento no histórico

- Nova entrada e nova saída abrem **sobre** a planilha.
- Topo: data em branco. Atalho na linha: data daquele dia.
- Fechar e Escape cancelam; clique no fundo não fecha.
- Depois de salvar, a planilha mostra o mês da data gravada.

## 005 — Edição, gráfico diário, login, mobile e docs

- Login/cadastro/troca de senha recusam e-mail fora do padrão; Enter no form válido equivale ao botão.
- **Editar** em Gerenciar (um item): modal preenchido, troca de tipo, PUT ou substituição, alcance se for recorrente; Gerenciar permanece aberto.
- Relatórios: três linhas a partir do histórico — saldo corrido, entradas do dia, saídas do dia.
- Layout retrato para iPhone 16 / 16 Pro Max (~393px e ~440px).
- README funcional; execução em `app-settings.md`; estas notas de versão.
