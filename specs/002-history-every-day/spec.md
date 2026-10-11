# Feature Specification: Histórico dia a dia e exclusão em lote

**Feature Branch**: `002-history-every-day`

**Created**: 2026-10-05

**Status**: Draft

**Input**: User description: "Atualmente o projeto apresenta na tela de histórico apenas as datas em que há movimentação. Eu preciso que mostre dia a dia em linhas mesmo que não haja movimentação, mantendo o saldo. A ideia é que o usuário consiga ver um horizonte da sua vida financeira. Quando os registros são feitos em histórico, os mesmos são somados entre as colunas de entradas, saídas, e saldo e para cada registro aparece um botão de excluir. Pensando em UX, quero que um modal seja aberto com a lista de registros (entradas e saídas) e a pessoa possa ir selecionar quais linhas quer apagar e clicar em um botão apagar. Ao fechar o modal, a tabela do histórico é atualizada."

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Linha para cada dia do período (Priority: P1)

O usuário abre o histórico financeiro no período que está visualizando e vê uma linha para **cada dia do calendário**, inclusive os dias em que não houve entrada nem saída. Nesses dias o saldo acumulado permanece o mesmo do dia anterior.

**Why this priority**: Sem a sequência contínua de dias, o histórico parece um extrato fragmentado e o usuário não consegue enxergar o horizonte da vida financeira.

**Independent Test**: Escolher um período de 10 dias com movimentação em apenas 2 datas e confirmar que aparecem 10 linhas, com saldo estável nos 8 dias sem movimento.

**Acceptance Scenarios**:

1. **Given** o usuário está autenticado e o período visível vai de 01/10 a 10/10, **When** só existem lançamentos em 03/10 e 07/10, **Then** o histórico exibe exatamente 10 linhas, uma para cada data de 01/10 a 10/10
2. **Given** o saldo ao fim de 02/10 é 1.000, **When** 03/10 não tem movimentação que altere saldo, **Then** a linha de 03/10 mostra saldo 1.000
3. **Given** o usuário navega para outro mês com o controle já existente de período, **When** o novo intervalo é exibido, **Then** todos os dias daquele intervalo aparecem em ordem cronológica, sem pular datas

---

### User Story 2 - Distinguir dias com e sem movimento (Priority: P2)

O usuário consegue, de relance, perceber quais dias tiveram lançamentos e quais são apenas continuidade do saldo, sem perder a leitura de planilha (entrada, saída e saldo).

**Why this priority**: Preencher todos os dias aumenta o volume de linhas; a leitura só permanece útil se o dia vazio não parecer um lançamento inexistente.

**Independent Test**: No mesmo período com dias mistos, identificar visualmente os dias sem movimento e confirmar que neles entrada e saída estão zeradas e o saldo foi carregado.

**Acceptance Scenarios**:

1. **Given** um dia sem lançamentos (nem pendentes de cartão, nem fatura, nem previsão recorrente), **When** o usuário olha essa linha, **Then** entrada e saída aparecem vazias ou zeradas e não há itens de transação naquele dia
2. **Given** um dia com lançamentos, **When** o usuário olha essa linha, **Then** vê os totais nas colunas e a lista dos lançamentos daquele dia (incluindo pendentes de cartão e previsões recorrentes), sem botão de excluir em cada item
3. **Given** um dia sem movimento entre dois dias com movimento, **When** o usuário percorre a lista, **Then** a linha vazia não interrompe a sequência de datas nem altera o saldo em relação ao dia anterior

---

### User Story 3 - Horizonte contínuo no passado e no futuro (Priority: P2)

Ao avançar ou voltar no histórico, o usuário continua vendo o calendário preenchido. No futuro, dias sem previsão ainda mostram o saldo projetado até então; no passado, dias sem registro mostram o saldo que vigorava naquela data.

**Why this priority**: A proposta é um horizonte de vida financeira, não só o mês corrente com movimento.

**Independent Test**: Navegar um mês para frente sem recorrências na maior parte dos dias e confirmar que cada dia aparece com o saldo projetado estável até a próxima previsão.

**Acceptance Scenarios**:

1. **Given** o usuário avança para um período futuro sem lançamentos previstos em vários dias, **When** visualiza o histórico, **Then** esses dias vazios aparecem com o saldo projetado trazido do último dia que teve efeito no saldo
2. **Given** o usuário volta para um período passado anterior a qualquer lançamento, **When** visualiza o histórico, **Then** cada dia do período aparece e o saldo permanece no valor de abertura (zero, se nunca houve movimento anterior)
3. **Given** existe um lançamento retroativo, **When** o usuário revisita os dias seguintes (com ou sem movimento), **Then** o saldo de todos esses dias reflete o recálculo cronológico, inclusive os dias vazios

---

### User Story 4 - Excluir lançamentos pela lista do dia (Priority: P1)

Na tabela do histórico, cada **dia** consolida totais de entrada, saída e saldo **e** lista os lançamentos daquele dia (tipo, valor e identificação). Esses lançamentos **não** exibem botão de excluir na planilha. O usuário abre um diálogo com a mesma lista daquele dia, marca quais linhas deseja remover e clica em “Apagar”. O sistema **sempre** pede confirmação (“tem certeza?”) antes de excluir. Se houver recorrentes na seleção, pergunta também **uma vez** o escopo e só então exclui. Se ainda restarem lançamentos naquele dia, o diálogo permanece aberto com a lista atualizada; se a lista ficar vazia, fecha sozinho.

**Why this priority**: Com uma linha por dia, vários lançamentos no mesmo dia poluem a planilha se cada um tiver o próprio botão de excluir; a seleção em lista reduz erro e deixa a leitura do horizonte mais limpa.

**Independent Test**: Num dia com duas entradas e uma saída, abrir o diálogo, marcar só a saída, apagar e verificar que a saída some da lista e da tabela, as duas entradas permanecem e o diálogo continua aberto.

**Acceptance Scenarios**:

1. **Given** um dia com um ou mais lançamentos, **When** o usuário aciona a ação de gerenciar/excluir daquela linha do histórico, **Then** abre um diálogo listando as entradas e saídas daquele dia (tipo, valor e identificação suficiente para reconhecer o registro)
2. **Given** o diálogo está aberto com vários itens, **When** o usuário marca dois itens, clica em “Apagar” e confirma que tem certeza, **Then** apenas os itens marcados são excluídos e os não marcados permanecem
3. **Given** o diálogo está aberto e nenhum item está marcado, **When** o usuário clica em “Apagar”, **Then** nada é excluído, não aparece “tem certeza?” e o sistema indica que é preciso selecionar ao menos um registro
3a. **Given** há itens marcados, **When** o usuário clica em “Apagar”, **Then** o sistema pede confirmação explícita antes de excluir; se o usuário recusar, nenhum lançamento some
4. **Given** o usuário concluiu uma exclusão e ainda restam lançamentos naquele dia, **When** a exclusão termina, **Then** o diálogo permanece aberto com a lista atualizada e a tabela do histórico ao fundo já reflete totais, saldo dos dias seguintes e ausência dos itens apagados
5. **Given** o usuário concluiu uma exclusão e não restou nenhum lançamento excluível naquele dia, **When** a exclusão termina, **Then** o diálogo fecha sozinho e a tabela mostra o dia vazio com saldo carregado
6. **Given** o usuário fecha o diálogo sem apagar, **When** o diálogo some, **Then** a tabela permanece (ou reexibe) o estado atual, sem perda de dados
7. **Given** um dia sem lançamentos excluíveis, **When** o usuário tenta abrir o diálogo, **Then** a ação não está disponível ou o diálogo deixa claro que não há registros para apagar
8. **Given** a planilha do histórico está visível, **When** o usuário olha os lançamentos nas linhas do dia, **Then** os lançamentos aparecem na própria linha do dia, mas não há botão de excluir por registro na tabela
9. **Given** o usuário marcou um não recorrente e dois recorrentes, clicou em “Apagar” e confirmou que tem certeza, **When** o sistema pede o escopo, **Then** a pergunta de escopo aparece uma vez; a escolha vale para os dois recorrentes e o não recorrente é excluído sem pergunta de escopo extra

---

### Edge Cases

- Período sem nenhum lançamento: todas as linhas do intervalo ainda são exibidas, com saldo constante (zero ou o saldo de abertura herdado de antes do intervalo)
- Primeiro dia do período: o saldo da linha é o saldo de abertura imediatamente anterior ao início do intervalo, mais os efeitos daquele dia
- Dia com apenas gasto de cartão pendente: a linha existe; o pendente aparece; o saldo da linha **não** muda por causa do pendente (comportamento já definido no produto)
- Dia de vencimento de fatura sem outros lançamentos: a linha não é “vazia”; o débito consolidado da fatura altera o saldo
- Intervalo longo (vários meses): todos os dias do intervalo visível continuam sendo listados; a navegação de período permanece o mecanismo para limitar o horizonte mostrado de uma vez
- Fins de semana e feriados: entram na sequência como qualquer outro dia; não são omitidos
- Diálogo de exclusão com seleção parcial: só os itens marcados saem; totais do dia e saldo seguinte recalculam só com o que restou
- Lançamento recorrente na lista: se o “Apagar” incluir um ou mais recorrentes, o sistema pergunta **uma vez** o escopo (só esta ocorrência / esta e as futuras / todas) e aplica essa escolha a todos os recorrentes marcados; os não recorrentes do mesmo clique são excluídos sem essa pergunta
- Previsão recorrente futura na lista: pode ser cancelada pela mesma regra de escopo; não se cria um “registro fantasma” só para apagar
- Lançamento derivado (fatura consolidada de cartão no vencimento): não entra como item selecionável para apagar; some ou se recalcula se o usuário apagar as saídas pendentes que a originaram
- Fechar o diálogo sem apagar: a tabela permanece reconciliada com o estado atual, sem perda de dados
- Após “Apagar” com itens restantes naquele dia: o diálogo não fecha; a lista e a tabela atualizam
- Após “Apagar” esvaziando o dia (e após confirmação): o diálogo fecha sozinho; o dia continua na planilha, agora vazio
- Recusar o “tem certeza?”: nenhum item da seleção é apagado; o diálogo de lista permanece como estava
- Exclusão de um único lançamento de um dia: o dia permanece na tabela (agora vazio), com saldo carregado do dia anterior

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O histórico financeiro DEVE exibir uma linha para cada data civil do período atualmente visível, do primeiro ao último dia, inclusive
- **FR-002**: O sistema NÃO DEVE omitir um dia do período visível só porque nele não há lançamentos
- **FR-003**: Em um dia sem efeito no saldo, a linha DEVE repetir o saldo acumulado do dia anterior (ou o saldo de abertura do período, se for o primeiro dia)
- **FR-004**: Em um dia sem lançamentos, as colunas de entrada e saída DEVEM aparecer sem valores de movimento (vazias ou zero), e a linha NÃO DEVE inventar transações
- **FR-005**: Em um dia com lançamentos, a linha DEVE mostrar os totais nas colunas e também listar os lançamentos daquele dia (incluindo pendentes de cartão e previsões recorrentes), atualizando o saldo exatamente como já definido para entradas confirmadas, saídas confirmadas, previsões recorrentes e fatura de cartão
- **FR-006**: O usuário DEVE conseguir distinguir um dia sem movimento de um dia com lançamentos sem sair da visão de planilha
- **FR-007**: A sequência de datas DEVE ser contínua e cronológica; não pode haver “buracos” de calendário dentro do período visível
- **FR-008**: Ao mudar o período visível (incluindo navegação para trás ou para frente), o preenchimento dia a dia DEVE se aplicar ao novo intervalo
- **FR-009**: O saldo exibido no topo DEVE permanecer consistente com o saldo da linha correspondente à data de referência do saldo atual (último dia já ocorrido no horizonte, não um dia futuro vazio escolhido ao acaso)
- **FR-010**: Esta regra de preenchimento aplica-se apenas à visão de histórico; relatórios e exportações existentes NÃO precisam passar a listar dias sem movimento, salvo se já o fizessem
- **FR-011**: Cada linha de dia do histórico DEVE consolidar nas colunas de entrada, saída e saldo os efeitos daquele dia; vários lançamentos no mesmo dia somam-se nessas colunas e também aparecem listados na linha do dia, sem virar cada um uma linha de calendário da planilha
- **FR-012**: A tabela do histórico NÃO DEVE apresentar botão de excluir em cada lançamento individual
- **FR-013**: O usuário DEVE poder abrir, a partir da linha do dia, um diálogo com a lista das entradas e saídas daquele dia
- **FR-014**: No diálogo, o usuário DEVE poder marcar um ou mais registros da lista e confirmar a exclusão com um único botão de apagar
- **FR-015**: O sistema NÃO DEVE apagar registros não marcados
- **FR-016**: Se o usuário confirmar apagar sem nenhuma seleção, o sistema DEVE recusar a ação, NÃO pedir “tem certeza?” e informar que é necessário selecionar ao menos um registro
- **FR-016a**: Com ao menos um item marcado, após “Apagar” o sistema DEVE pedir confirmação explícita (“tem certeza?”) antes de excluir, inclusive quando não há recorrente; se o usuário recusar, NENHUM lançamento é excluído
- **FR-017**: Após cada exclusão bem-sucedida, o histórico visível DEVE atualizar imediatamente (itens, totais do dia, saldo dos dias seguintes e saldo do topo, quando aplicável). Se ainda houver lançamentos excluíveis naquele dia, o diálogo DEVE permanecer aberto com a lista atualizada; se não restar nenhum, o diálogo DEVE fechar sozinho. Fechar o diálogo sem apagar também DEVE deixar a tabela no estado atual
- **FR-018**: Apenas lançamentos de entrada e saída criados ou previstos para o usuário DEVEM ser selecionáveis para exclusão; itens puramente calculados (como fatura consolidada de cartão) NÃO DEVEM ser apagáveis por essa lista
- **FR-019**: A ordem do fluxo DEVE ser: “Apagar” → confirmação “tem certeza?” → se a seleção incluir recorrente, **um** diálogo de escopo (somente esta / esta e futuras / todas) aplicado a todos os recorrentes marcados → exclusão. Itens não recorrentes do mesmo clique não recebem diálogo de escopo. Recusar o “tem certeza?” cancela o fluxo inteiro.

### Key Entities

- **Dia do histórico**: Uma data civil dentro do período visível; pode conter zero ou mais lançamentos do dia; sempre possui saldo acumulado ao fim daquele dia
- **Período visível**: Intervalo de datas que o usuário está olhando no histórico (o mesmo recorte já usado para navegar no passado e no futuro)
- **Saldo de abertura do período**: Saldo acumulado imediatamente antes do primeiro dia do período visível; base para a primeira linha, inclusive se esse dia for vazio
- **Lista de lançamentos do dia**: Conjunto de entradas e saídas (incluindo previsões recorrentes daquela data) apresentado no diálogo de exclusão; cada item pode ser marcado independentemente
- **Seleção para exclusão**: Subconjunto marcado pelo usuário na lista; só esse subconjunto é removido ao confirmar “Apagar”

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em qualquer período visível de N dias, o histórico mostra exatamente N linhas de data, uma por dia civil, em 100% dos casos de teste
- **SC-002**: Em 100% dos dias sem movimento, o saldo da linha é idêntico ao saldo do dia anterior (ou ao saldo de abertura, no primeiro dia)
- **SC-003**: O usuário identifica em menos de 5 segundos, ao varrer um mês misto, quais dias tiveram movimento e quais só carregam saldo
- **SC-004**: Após um lançamento retroativo, 100% das linhas seguintes do período visível (incluindo vazias) mostram saldo coerente com a ordem cronológica
- **SC-005**: O usuário percorre um horizonte de pelo menos 90 dias contínuos (passado ou futuro) sem datas faltando e sem o saldo “resetar” nos dias vazios
- **SC-006**: A troca de período (mês seguinte ou anterior) continua utilizável: a nova faixa aparece completa dia a dia sem o usuário precisar procurar datas omitidas
- **SC-007**: Em um dia com 3 ou mais lançamentos, o usuário consegue excluir um subconjunto escolhido abrindo a lista, clicando em “Apagar” e confirmando; não usa botão de excluir por item na tabela
- **SC-008**: Após uma exclusão pela lista, 100% das linhas do período visível (incluindo dias vazios) mostram totais e saldo coerentes com os lançamentos restantes, sem o usuário precisar recarregar a tela manualmente
- **SC-009**: 100% das tentativas de “Apagar” sem seleção são recusadas e nenhum lançamento some

## Assumptions

- O “horizonte” é o período que o usuário já escolhe na tela de histórico, não um calendário infinito sem recorte
- Dias sem movimento não geram registros persistidos; são apenas apresentados na visão
- O saldo carregado em dia vazio segue as mesmas regras de negócio já existentes (pendente de cartão não reduz saldo; fatura no vencimento reduz; previsões recorrentes nos dias em que caem alteram o saldo)
- Relatórios, CSV e cadastro de lançamentos permanecem como estão, exceto pelo efeito visual/lógico do histórico contínuo
- Esta especificação substitui a premissa anterior de que dias sem transação podiam ser omitidos do histórico
- O diálogo de exclusão é por **dia** (a linha da planilha), não uma lista única de todo o período visível
- A exclusão em lote não altera cadastros de categoria, fonte ou método de pagamento
- Fechar o diálogo sempre devolve o usuário à tabela já reconciliada com o estado atual dos dados; “Apagar” com itens restantes não fecha o diálogo
- A regra de escopo de recorrência (somente esta / esta e futuras / todas) permanece a mesma do produto; neste diálogo a pergunta de escopo ocorre **uma vez por clique em Apagar**, cobrindo todos os recorrentes marcados

## Clarifications

### Session 2026-10-05 (acréscimo à spec)

- Q: A exclusão em lote lista o dia ou o período inteiro? → A: Lista as entradas e saídas **daquele dia**, aberta a partir da linha da planilha
- Q: Cada lançamento continua com botão de excluir na tabela? → A: Não; a tabela só consolida totais; a exclusão acontece no diálogo por seleção
- Q: Fatura consolidada de cartão pode ser apagada nessa lista? → A: Não; só entradas e saídas do usuário (incluindo previsões recorrentes, com o diálogo de escopo já existente)

### Session 2026-10-05

- Q: Na linha de cada dia, os lançamentos ainda aparecem na tabela ou só os totais? → A: Totais na linha **e** lista dos lançamentos do dia visível na tabela, sem excluir por item
- Q: Depois de “Apagar” com sucesso, o diálogo fecha ou permanece aberto? → A: Permanece aberto enquanto houver itens naquele dia; fecha sozinho quando a lista ficar vazia
- Q: Vários recorrentes no mesmo “Apagar”: um escopo para todos ou um por item? → A: Um único diálogo de escopo para todos os recorrentes daquele clique; não recorrentes do mesmo clique são só apagados
- Q: Além de “Apagar” (e do escopo, se houver recorrente), pede “tem certeza?” extra? → A: Sempre um “tem certeza?” depois de “Apagar”, mesmo sem recorrente; recusar cancela a exclusão

