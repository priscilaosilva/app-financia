# Financia

Gestão financeira pessoal: saldo, histórico diário, entradas e saídas (inclusive recorrentes), relatórios e perfil. Interface em português.

Como subir API e frontend: [app-settings.md](./app-settings.md).  
O que mudou em cada entrega: [RELEASE-NOTES.md](./RELEASE-NOTES.md).

## Conta e sessão

- Cadastro, login e troca de senha (e-mail da conta + senha nova duas vezes).
- E-mail precisa estar no formato `nome@dominio`; Enter no formulário válido dispara a mesma ação do botão.
- A sessão vale enquanto o token JWT for aceito. Trocar a senha invalida tokens antigos: é preciso entrar de novo.

## Histórico

- Planilha de **todos os dias do calendário** do mês, com entrada, saída e saldo corrido.
- Recorrências futuras aparecem projetadas; cartão de crédito pode marcar saída como pendente.
- **Nova entrada / Nova saída** no topo abrem um modal com data em branco. Os atalhos na linha pré-preenchem a data daquele dia. Escape ou Fechar cancela; clique no fundo não fecha.
- **Gerenciar** na linha lista os lançamentos apagáveis daquele dia. Com **exatamente um** item marcado, aparece **Editar**.
- Edição usa o mesmo tipo de modal: dá para alterar data, valor, descrição, fonte/categoria/pagamento e o tipo (entrada ↔ saída). Mesmo tipo atualiza; tipo diferente substitui (não ficam dois lançamentos). Lançamento recorrente pergunta se vale só esta ocorrência, esta e as futuras, ou toda a série. Depois de salvar, Gerenciar permanece aberto no mesmo dia (fecha só se não restar nada para gerenciar).

## Relatórios

- Filtro por mês, ano ou intervalo.
- Totais, categorias e exportação CSV.
- Gráfico de linhas: **saldo corrido**, **entradas do dia** e **saídas do dia** (não acumulado de entradas/saídas).

## Configurações e perfil

- Fontes de receita, categorias, formas de pagamento (incluindo cartão com ciclo de fatura).
- Tema claro/escuro no perfil.

## Telas estreitas

Em largura de telemóvel (~393px e ~440px), a navegação vai para o topo, a tabela do histórico rola por dentro do card e os modais rolam internamente, sem exigir scroll horizontal da página.
