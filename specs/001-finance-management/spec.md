# Feature Specification: Sistema de Gestão Financeira

**Feature Branch**: `001-finance-management`

**Created**: 2026-10-04

**Status**: Draft

**Input**: User description: "Preciso de um sistema de gestão financeira onde o usuário registra entradas e saídas do seu financeiro..."

---

## User Scenarios & Testing *(mandatory)*

<!--
  User stories ordered by priority. Each story is independently testable and delivers
  standalone value even without the others being implemented.
-->

### User Story 1 - Registro e Visualização de Saldo (Priority: P1)

O usuário acessa o sistema, visualiza seu saldo total atualizado e navega pelo histórico financeiro em formato de planilha, onde cada linha representa uma data com suas transações, colunas de entrada, saída e saldo acumulado.

**Why this priority**: É o núcleo do sistema — sem saldo e histórico visíveis e corretos, nenhuma outra funcionalidade agrega valor ao usuário.

**Independent Test**: Registrar ao menos uma entrada e uma saída e verificar que o histórico exibe as datas corretamente, com saldo sequencial acumulado e total no topo consistente com a última linha.

**Acceptance Scenarios**:

1. **Given** o usuário está autenticado, **When** acessa a visão histórica, **Then** vê uma lista de datas com colunas "Entrada", "Saída" e "Saldo", onde cada linha representa um dia que possui transações
2. **Given** há múltiplas transações em datas diferentes, **When** o usuário visualiza o histórico, **Then** o saldo de cada linha acumula corretamente todas as entradas e saídas em ordem cronológica
3. **Given** o usuário está na visão histórica, **When** clica na seta "avançar", **Then** visualiza datas futuras com os lançamentos recorrentes projetados
4. **Given** o usuário está na visão histórica, **When** clica na seta "voltar", **Then** visualiza datas passadas com os registros históricos reais
5. **Given** há transações registradas, **When** o usuário visualiza o saldo total no topo, **Then** o valor é idêntico ao saldo da última linha do histórico

---

### User Story 2 - Registro de Entradas de Dinheiro (Priority: P1)

O usuário registra uma entrada financeira informando data, valor, fonte de receita (opcional) e descrição livre (opcional). Pode gerenciar fontes de receita customizadas como "empresa X", "vendas de bolo".

**Why this priority**: Sem registro de entradas, o sistema não tem dados para calcular saldo ou apresentar histórico.

**Independent Test**: Registrar uma entrada com data, valor, fonte e descrição; verificar que aparece no histórico na data correta com o saldo atualizado.

**Acceptance Scenarios**:

1. **Given** o usuário acessa o formulário de nova entrada, **When** preenche data, valor, fonte e descrição e confirma, **Then** a entrada aparece no histórico na data selecionada e o saldo total é atualizado
2. **Given** o usuário cadastra uma nova fonte de receita (ex: "vendas de bolo"), **When** registra uma entrada, **Then** a nova fonte está disponível para seleção no formulário
3. **Given** o usuário omite os campos opcionais (fonte e descrição), **When** salva a entrada apenas com data e valor, **Then** o registro é criado com sucesso
4. **Given** o usuário não informa data ou valor, **When** tenta salvar, **Then** o sistema exibe erro indicando quais campos são obrigatórios e não salva o registro

---

### User Story 3 - Registro de Saídas com Categorias e Fontes de Pagamento (Priority: P1)

O usuário cadastra categorias de gastos e fontes de pagamento. Ao registrar uma saída, seleciona categoria, fonte de pagamento, valor e descrição. Fontes do tipo "cartão de crédito" acumulam gastos e os contabilizam no dia de vencimento.

**Why this priority**: Controle de saídas é a metade essencial do sistema financeiro; sem isso o saldo não reflete a realidade.

**Independent Test**: Cadastrar categoria "lazer", fonte "cartão de crédito" com período e vencimento definidos, registrar uma saída e verificar que aparece contabilizada no dia de vencimento no histórico.

**Acceptance Scenarios**:

1. **Given** o usuário cadastrou categoria "lazer" e fonte "conta corrente", **When** registra uma saída com data, valor e esses dados, **Then** a saída aparece no histórico na data informada e o saldo é reduzido
1a. **Given** o usuário tenta salvar uma saída sem informar data ou valor, **When** confirma, **Then** o sistema exibe erro indicando os campos obrigatórios e não salva o registro
2. **Given** o usuário cadastrou um cartão de crédito com vencimento dia 10 e período de faturamento 11/set a 10/out, **When** registra uma saída com data dentro do período, **Then** o gasto aparece no histórico na data do registro marcado visualmente como "pendente — cartão de crédito", sem afetar o saldo; no dia 10/out o total acumulado do cartão é debitado do saldo como um único lançamento
3. **Given** o usuário visualiza o histórico, **When** há gastos de cartão registrados no período mas antes do vencimento, **Then** esses gastos são visivelmente diferenciados (ex: cor, ícone ou rótulo) dos lançamentos que já afetaram o saldo
4. **Given** o usuário cria uma fonte "cartão de crédito", **When** não informa data de vencimento ou período de faturamento, **Then** o sistema exibe erro e não salva a fonte
5. **Given** o usuário acessa a gestão de categorias, **When** cria uma nova categoria (ex: "financiamento"), **Then** ela fica disponível para seleção ao registrar saídas

---

### User Story 4 - Transações Recorrentes (Priority: P2)

O usuário configura uma entrada ou saída como recorrente com frequência diária, mensal ou anual. O sistema projeta automaticamente os lançamentos futuros no histórico.

**Why this priority**: Recorrência é essencial para planejamento financeiro (ex: salário mensal, aluguel), mas pode ser implementada após o core de registro avulso.

**Independent Test**: Cadastrar uma saída mensal recorrente de "aluguel" e verificar que aparece nos próximos 12 meses na visão futura do histórico.

**Acceptance Scenarios**:

1. **Given** o usuário cria uma saída mensal recorrente de "aluguel" R$ 1.200, **When** navega para meses futuros no histórico, **Then** a saída aparece no mesmo dia de cada mês subsequente
2. **Given** o usuário cria uma entrada diária recorrente, **When** navega pelo histórico, **Then** a entrada aparece em todos os dias cobertos pelo período de recorrência
3. **Given** o usuário cria um lançamento recorrente anual, **When** navega para o mesmo período no ano seguinte, **Then** o lançamento aparece projetado
4. **Given** um lançamento recorrente existe, **When** o usuário edita ou cancela uma ocorrência, **Then** o sistema apresenta um diálogo com três opções: (a) somente esta ocorrência, (b) esta e todas as futuras, (c) todas as ocorrências (passadas e futuras); a ação só é aplicada após o usuário confirmar sua escolha
5. **Given** o usuário escolhe "somente esta ocorrência" ao editar, **When** confirma, **Then** apenas aquela data é alterada e as demais ocorrências permanecem inalteradas
6. **Given** o usuário escolhe "esta e todas as futuras" ao editar, **When** confirma, **Then** a recorrência original é encerrada naquela data e uma nova série começa com os novos valores a partir da próxima ocorrência

---

### User Story 5 - Painel de Relatórios com Exportação (Priority: P3)

O usuário acessa a seção de relatórios, seleciona um período (intervalo de datas, mês fechado ou ano fechado) e visualiza em tela um painel consolidado com totais, projeções e agrupamentos por categoria. Pode exportar os dados em CSV.

**Why this priority**: Relatórios são funcionalidade complementar ao núcleo; o sistema já entrega valor completo sem eles em v1.

**Independent Test**: Selecionar um mês fechado, verificar que os totais de entradas, saídas e economias exibidos na tela correspondem ao somatório do histórico do mesmo período, e exportar CSV com os mesmos dados.

**Acceptance Scenarios**:

1. **Given** o usuário acessa a seção "Relatórios", **When** seleciona o filtro "mês fechado" (ex: setembro/2026), **Then** o painel exibe na tela: total de entradas, total de saídas, total por cartão de crédito, total por categoria de gasto, projeção de gastos ao longo do mês, projeção de entradas ao longo do mês e projeção de economia do período
2. **Given** o usuário acessa a seção "Relatórios", **When** seleciona filtro por intervalo de datas (data início e data fim), **Then** o painel exibe os mesmos indicadores consolidados para o intervalo selecionado
3. **Given** o usuário acessa a seção "Relatórios", **When** seleciona filtro "ano fechado" (ex: 2026), **Then** o painel exibe os indicadores consolidados para o ano inteiro
4. **Given** o painel de relatório está exibido, **When** o usuário aciona "Exportar CSV", **Then** o sistema disponibiliza o download de um arquivo CSV com os dados do relatório exibido em tela
5. **Given** o usuário não seleciona nenhum período, **When** tenta gerar o relatório, **Then** o sistema solicita que um período seja informado antes de exibir os dados
6. **Given** o painel de relatório está exibido, **When** o usuário visualiza o agrupamento por categorias, **Then** cada categoria cadastrada pelo usuário aparece com seu total de gastos e percentual em relação ao total de saídas do período

---

### Edge Cases

- ✅ Vencimento do cartão é contabilizado sempre no dia configurado, independente de ser fim de semana ou feriado (FR-007)
- ✅ Lançamentos retroativos recalculam o saldo de todos os dias posteriores à data informada (FR-011)
- ✅ Ao excluir categoria ou fonte com transações vinculadas: o nome é preservado como texto fixo nas transações históricas ("órfão"), mas a opção some das listas de seleção para novos lançamentos (FR-022)
- Dias sem nenhuma transação aparecem no histórico ou são omitidos?
- O que acontece se dois cartões de crédito tiverem o mesmo dia de vencimento?

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema DEVE exibir o saldo total do usuário atualizado em tempo real após cada transação registrada
- **FR-002**: O usuário DEVE poder registrar uma entrada financeira com: data (obrigatório), valor (obrigatório), fonte de receita (opcional), descrição em texto livre (opcional)
- **FR-003**: O usuário DEVE poder cadastrar, editar e remover fontes de receita customizadas (ex: "empresa X", "pagamento de amigos", "vendas de bolo")
- **FR-004**: O usuário DEVE poder cadastrar, editar e remover categorias de gastos customizadas (ex: "lazer", "contas fixas", "aluguel", "financiamento")
- **FR-005**: O usuário DEVE poder cadastrar, editar e remover fontes de pagamento de três tipos: conta corrente, reserva e cartão de crédito
- **FR-006**: Ao cadastrar uma fonte do tipo "cartão de crédito", o sistema DEVE exigir: data de vencimento (dia do mês), data de início e data de fim do período de faturamento
- **FR-007**: Saídas associadas a um cartão de crédito DEVEM aparecer no histórico na data do registro, marcadas visualmente como "pendente — cartão de crédito", sem afetar o saldo; no dia de vencimento (sempre o dia configurado, independente de fim de semana ou feriado), o total acumulado do cartão é debitado do saldo como um único lançamento consolidado
- **FR-008**: O usuário DEVE poder registrar uma saída financeira com: data (obrigatório), valor (obrigatório), categoria (opcional), fonte de pagamento (opcional), descrição em texto livre (opcional)
- **FR-009**: O sistema DEVE suportar configuração de recorrência em transações (entrada ou saída) com frequência: diária, mensal ou anual
- **FR-009a**: Ao editar ou cancelar uma ocorrência de lançamento recorrente, o sistema DEVE apresentar um diálogo com três opções: (a) somente esta ocorrência, (b) esta e todas as futuras, (c) todas as ocorrências; a ação só é aplicada após o usuário confirmar sua escolha
- **FR-010**: O histórico financeiro DEVE ser exibido em formato de planilha com linhas por data e colunas: Entrada, Saída e Saldo
- **FR-011**: O saldo em cada linha do histórico DEVE ser calculado dinamicamente a partir de todas as transações em ordem cronológica — um lançamento com data retroativa recalcula o saldo de todos os dias posteriores a ele
- **FR-012**: O usuário DEVE poder navegar no histórico entre períodos passados e futuros usando controles de navegação (setas ou equivalente)
- **FR-013**: Datas futuras com transações recorrentes DEVEM aparecer como previsão no histórico ao navegar para o futuro
- **FR-014**: O sistema DEVE possuir autenticação de usuário com tela de login como único ponto de entrada; usuários não autenticados não devem acessar nenhuma outra tela
- **FR-015**: O sistema DEVE apresentar menu de navegação lateral com acesso a: Configurações do sistema, Perfil do usuário, Histórico financeiro e Relatórios
- **FR-021**: A seção "Configurações do sistema" DEVE conter: (a) preferências da conta — nome, email, senha e exclusão de conta; (b) gerenciamento de cadastros auxiliares — categorias de gastos, fontes de receita e fontes de pagamento; (c) preferência de tema visual — modo claro ou modo escuro (dark mode)
- **FR-022**: Ao excluir uma categoria ou fonte de pagamento/receita que possui transações vinculadas, o sistema DEVE preservar o nome como texto fixo nas transações históricas existentes e remover a opção apenas das listas de seleção para novos lançamentos; as transações históricas NÃO devem ser alteradas ou excluídas
- **FR-016**: O sistema DEVE apresentar um painel de relatórios em tela com os dados consolidados do período selecionado
- **FR-017**: O painel de relatórios DEVE suportar três tipos de filtro de período: intervalo de datas (data início + data fim), mês fechado e ano fechado
- **FR-018**: O painel de relatórios DEVE exibir os seguintes indicadores: total de entradas, total de saídas, total de gastos em cartão de crédito, agrupamento de saídas por categoria (com valor e percentual), projeção de gastos ao longo do período, projeção de entradas ao longo do período e projeção de economia do período
- **FR-019**: O painel de relatórios DEVE agrupar as saídas pelas categorias cadastradas pelo usuário, exibindo total e percentual de cada categoria em relação ao total de saídas do período
- **FR-020**: O usuário DEVE poder exportar os dados do relatório exibido em tela no formato CSV

### Key Entities *(include if feature involves data)*

- **Usuário**: Possui credenciais de acesso e perfil; é proprietário isolado de todos os dados financeiros abaixo
- **Entrada (Income)**: Data, valor, fonte de receita (opcional), descrição (opcional), configuração de recorrência (opcional)
- **Saída (Expense)**: Data (obrigatório), valor (obrigatório), categoria de gasto (opcional), fonte de pagamento (opcional), descrição (opcional), configuração de recorrência (opcional)
- **Fonte de Receita**: Nome definido pelo usuário (ex: "empresa X", "vendas de bolo"); pertence ao usuário
- **Categoria de Gasto**: Nome definido pelo usuário (ex: "lazer", "aluguel"); pertence ao usuário
- **Fonte de Pagamento**: Tipo (conta corrente | reserva | cartão de crédito), nome; se cartão: dia de vencimento, data de início e fim do período de faturamento; pertence ao usuário
- **Recorrência**: Frequência (diária | mensal | anual), data de início, data de término (opcional); associada a uma entrada ou saída
- **Relatório**: Parâmetros de período (tipo de filtro + datas), indicadores calculados (totais, projeções, agrupamentos por categoria); gerado sob demanda, não persistido

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O usuário consegue registrar uma entrada ou saída completa em menos de 60 segundos
- **SC-002**: O histórico exibe saldo acumulado correto para 100% das transações registradas, sem divergência em relação ao saldo total exibido no topo
- **SC-003**: Transações recorrentes são projetadas e exibidas corretamente para pelo menos 24 meses no futuro
- **SC-004**: O usuário consegue navegar pelo histórico de qualquer período passado sem erro ou perda de dados
- **SC-005**: Gastos de cartão de crédito são contabilizados no dia de vencimento correto em 100% dos casos
- **SC-006**: 90% dos novos usuários conseguem registrar sua primeira transação sem necessidade de suporte ou documentação externa
- **SC-007**: O sistema permanece responsivo (sem lentidão perceptível) com até 5 anos de histórico de transações de um único usuário

---

## Clarifications

### Session 2026-10-04 (especificação inicial)

- Q: Ao editar/cancelar uma ocorrência de lançamento recorrente, qual escopo de alteração deve ser oferecido? → A: O sistema pergunta ao usuário no momento da edição, apresentando as três opções: somente esta ocorrência / esta e todas as futuras / todas as ocorrências
- Q: Em qual formato o relatório financeiro deve ser exportado? → A: CSV para exportação; o relatório também deve ser apresentado em tela com indicadores de totais por categoria, projeções de gastos, entradas e economia; filtros por período livre, mês fechado e ano fechado
- Q: Como gastos de cartão de crédito aparecem no histórico entre a data de registro e o vencimento? → A: Aparecem na data de registro marcados como "pendente — cartão de crédito" sem afetar o saldo; no vencimento o total é debitado como único lançamento

### Session 2026-10-04 (clarify)

- Q: Ao registrar uma saída, o usuário precisa informar uma data para ela? → A: Sim, data é obrigatória nas saídas — assim como nas entradas, o usuário informa a data da despesa, permitindo lançamentos retroativos
- Q: Quando o usuário registra uma transação com data passada, o sistema recalcula o saldo de todos os dias posteriores a ela? → A: Sim, o saldo é sempre recalculado dinamicamente em ordem cronológica — um lançamento retroativo afeta o saldo de todos os dias seguintes
- Q: Quando o vencimento de um cartão de crédito cai em um final de semana ou feriado, o débito é contabilizado no mesmo dia ou deslocado? → A: No mesmo dia configurado — feriados e fins de semana não deslocam o vencimento
- Q: O que o usuário encontra na seção "Configurações do sistema" do menu lateral? → A: Preferências da conta (nome, email, senha, exclusão de conta) + gerenciamento de cadastros auxiliares (categorias, fontes de receita, fontes de pagamento) + seleção de tema visual (modo claro ou dark mode)
- Q: Quando o usuário exclui uma categoria ou fonte de pagamento vinculada a transações existentes, o que acontece com essas transações? → A: As transações históricas preservam o nome como texto fixo ("órfão") e a opção some das listas de seleção para novos lançamentos; nenhuma transação é alterada ou excluída

- Q: Ao editar/cancelar uma ocorrência de lançamento recorrente, qual escopo de alteração deve ser oferecido? → A: O sistema pergunta ao usuário no momento da edição, apresentando as três opções: somente esta ocorrência / esta e todas as futuras / todas as ocorrências
- Q: Em qual formato o relatório financeiro deve ser exportado? → A: CSV para exportação; o relatório também deve ser apresentado em tela com indicadores de totais por categoria, projeções de gastos, entradas e economia; filtros por período livre, mês fechado e ano fechado
- Q: Como gastos de cartão de crédito aparecem no histórico entre a data de registro e o vencimento? → A: Aparecem na data de registro marcados como "pendente — cartão de crédito" sem afetar o saldo; no vencimento o total é debitado como único lançamento

---

## Assumptions

- O sistema é **single-tenant por usuário**: cada usuário vê e gerencia exclusivamente seus próprios dados
- O acesso é via **navegador web desktop**; dispositivos móveis não são prioridade desta versão
- A moeda padrão é o **Real Brasileiro (BRL)**; suporte a múltiplas moedas está fora do escopo desta versão
- Todos os lançamentos são **manuais**; não há integração automática com bancos ou importação de extratos nesta versão
- O sistema de autenticação será **email e senha**; SSO, OAuth social e autenticação multifator estão fora do escopo desta versão
- A tecnologia definida pelo stakeholder é: **frontend Angular**, **backend Spring Boot**, **banco de dados MySQL**
- Dias sem transações podem ser omitidos do histórico (exibir apenas dias com movimento), a menos que o usuário esteja navegando para uma data específica
- Ao excluir uma fonte de pagamento ou categoria que possui transações históricas, as transações existentes preservam o nome da fonte/categoria como texto fixo ("órfão") e a opção é removida apenas das listas de seleção para novos lançamentos (FR-022)
