# Feature Specification: Edição no histórico, gráfico de evolução, login e documentação

**Feature Branch**: `005-edit-mobile-docs`

**Created**: 2026-10-11

**Status**: Draft

**Input**: User description: "Tela de login: formatação no campo e-mail e bloqueio caso esteja fora do padrão de e-mail; ao apertar tecla Enter, tentar entrar no sistema. Tela relatórios: gráfico de linha no período selecionado com saldo (mesmo projeção futura), entradas e saídas. Tela histórico: em Gerenciar, botão para editar quando só 1 item estiver selecionado; modal semelhante ao de nova entrada/saída, preenchido, com opção de mudar de saída para entrada (e o contrário) mantendo os preenchimentos. Frontend: responsividade para iPhone 16 e 16 Pro Max se possível. Release notes das features até então e padrão para próximos desenvolvimentos. Instruções de como rodar saem do README para app-settings.md. README vira documentação funcional do Financia."

## Clarifications

### Session 2026-10-11

- Q: Depois de salvar a edição com sucesso, o que o sistema faz com o modal Gerenciar? → A: Fecha só a edição; Gerenciar continua aberto com a lista do dia atualizada
- Q: No gráfico de linhas, entradas e saídas são o valor de cada dia ou o acumulado no período? → A: Cada ponto é o total daquele dia (como as colunas da planilha)
- Q: Se o item editado faz parte de uma repetição, o que a alteração vale? → A: Perguntar (só este / a partir daqui / toda a série), como na exclusão

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Entrar com e-mail válido e com a tecla Enter (Priority: P1)

Na tela de login, a pessoa informa o e-mail num formato reconhecível (como `nome@dominio`). Se o texto não for um e-mail, o sistema **não tenta entrar** e explica o problema. Com e-mail e senha preenchidos de forma válida, a tecla **Enter** dispara a mesma ação do botão **Entrar**.

**Why this priority**: É a porta de entrada de todo o produto; erro de digitação e teclado são o primeiro atrito.

**Independent Test**: Colocar e-mail sem `@` e tentar Entrar/Enter — permanece no login. Corrigir para um e-mail válido, Enter — tenta autenticar como o botão.

**Acceptance Scenarios**:

1. **Given** a pessoa está no login, **When** o e-mail está vazio ou não parece e-mail (sem `@`, sem domínio), **Then** **Entrar** permanece bloqueado (ou a tentativa é recusada) e há indicação clara no campo
2. **Given** e-mail válido e senha com o mínimo já exigido, **When** pressiona Enter no campo de e-mail ou de senha, **Then** o sistema tenta entrar (mesmo efeito de clicar em **Entrar**)
3. **Given** e-mail inválido, **When** pressiona Enter, **Then** não envia a tentativa de sessão; a pessoa continua no login
4. **Given** cadastro ou troca de senha esquecida nessa mesma tela, **When** o e-mail está fora do padrão, **Then** o envio desses fluxos também é bloqueado pelo mesmo critério de e-mail

---

### User Story 2 - Editar um lançamento a partir de Gerenciar (Priority: P1)

No histórico, **Gerenciar** abre a lista do dia. Se a pessoa marca **exatamente um** item, aparece **Editar**. O formulário abre como o de nova entrada/saída, já preenchido. Ela pode mudar o tipo (entrada ↔ saída) sem perder o que for comum (data, valor, descrição e, se houver, a intenção de repetir). Se o item for de uma repetição, ao salvar o sistema pergunta o alcance (**só este** / **a partir daqui** / **toda a série**), como na exclusão. Ao concluir, o dia e o saldo refletem a alteração.

**Why this priority**: Hoje só se apaga; corrigir um lançamento errado é o ganho de produto desta feature.

**Independent Test**: Criar uma saída, Gerenciar, selecionar só ela, Editar, mudar valor, salvar; repetir mudando o tipo para entrada e conferir a linha.

**Acceptance Scenarios**:

1. **Given** o modal de lançamentos do dia está aberto e nenhum ou mais de um item está marcado, **When** a pessoa olha as ações, **Then** **Editar** não está disponível
2. **Given** exatamente um item apagável está marcado, **When** clica em **Editar**, **Then** abre um modal de registro no mesmo estilo de nova entrada/saída, com os campos daquele lançamento preenchidos (data, valor, descrição, fonte/categoria/pagamento quando existirem, repetir se for o caso)
3. **Given** o modal de edição está aberto numa saída, **When** troca o tipo para entrada, **Then** o formulário passa a ser o de entrada **sem apagar** data, valor e descrição já preenchidos; campos só de saída (categoria, fonte de pagamento) deixam de aplicar; campos só de entrada (fonte de receita) começam vazios para ela escolher
4. **Given** o modal de edição está aberto numa entrada, **When** troca o tipo para saída, **Then** data, valor e descrição permanecem; fonte de receita não se aplica; categoria e fonte de pagamento começam vazios
5. **Given** a edição foi salva com sucesso (e, se recorrente, o alcance foi escolhido), **When** o histórico atualiza, **Then** o item antigo daquele dia não permanece como estava: vale o tipo e os dados novos; o modal de edição fecha; **Gerenciar** continua aberto com a lista do dia já atualizada (seleção limpa)
6. **Given** Fechar/Cancelar ou Escape no modal de edição, **When** sai sem salvar, **Then** o lançamento original não muda (mesmo critério de fechar o modal de nova entrada/saída: fundo não descarta)
7. **Given** o item editado é recorrente (ou a pessoa marcou repetir), **When** confirma o salvamento, **Then** aparece a mesma escolha de alcance da exclusão (só este / a partir daqui / toda a série) antes de gravar; cancelar essa pergunta não aplica a edição

---

### User Story 3 - Ver saldo, entradas e saídas em linhas no relatório (Priority: P2)

Depois de gerar o relatório do período escolhido (mês, ano ou intervalo), a pessoa vê um **gráfico de linhas** com três séries no mesmo recorte: **saldo** corrido (incluindo dias futuros projetados, no mesmo espírito do histórico contínuo), **entradas do dia** e **saídas do dia** (como as colunas da planilha, não acumulado).

**Why this priority**: Os totais já existem; falta a evolução no tempo no recorte que ela já selecionou.

**Independent Test**: Gerar relatório de um mês com movimento; identificar as três linhas. Gerar um período que inclua datas futuras e ver a linha de saldo continuar (projeção), não sumir.

**Acceptance Scenarios**:

1. **Given** um relatório gerado com sucesso, **When** a pessoa olha a área de gráficos, **Then** há um gráfico de linhas com saldo corrido, entradas do dia e saídas do dia em cada data do período do filtro
2. **Given** o período inclui dias ainda não ocorridos, **When** há projeção de saldo (recorrências etc., como no histórico), **Then** a linha de saldo segue nesses dias; não se limita ao “já realizado”; entradas/saídas projetadas daquele dia entram no ponto do dia
3. **Given** o período não tem movimento, **When** o relatório é gerado, **Then** o gráfico ainda aparece (entradas e saídas em zero; saldo constante), sem quebrar a tela
4. **Given** a pessoa muda o filtro e gera de novo, **When** o novo relatório chega, **Then** o gráfico passa a refletir só esse período

---

### User Story 4 - Usar o Financia no tamanho de iPhone 16 e 16 Pro Max (Priority: P2)

As telas principais (login, histórico, relatórios, configurações, perfil) e os modais de registro/gerenciar cabem e se usam em aparelhos no tamanho do **iPhone 16** e do **iPhone 16 Pro Max**, sem precisar “arrastar a página para o lado” para achar botão ou coluna essencial.

**Why this priority**: O produto hoje é pensado em desktop; o pedido é viabilizar esses dois telefones se o layout atual permitir ajustes.

**Independent Test**: Abrir login e histórico nessas duas larguras; registrar pelo topo; abrir Gerenciar; gerar relatório e ver o gráfico; nenhum fluxo principal exige scroll horizontal da página.

**Acceptance Scenarios**:

1. **Given** a largura típica de iPhone 16, **When** a pessoa percorre login, histórico, relatórios, configurações e perfil, **Then** conteúdo essencial (saldo, ações Nova entrada/saída, planilha ou equivalente empilhado, gráfico, formulários) permanece acessível sem scroll horizontal da página
2. **Given** a largura típica de iPhone 16 Pro Max, **When** faz o mesmo percurso, **Then** o mesmo critério se aplica (aproveitando o pouco de espaço extra, sem quebrar)
3. **Given** um modal de nova entrada, edição ou Gerenciar no telefone, **When** está aberto, **Then** formulário e botões de fechar/salvar cabem na área visível com scroll **dentro** do modal se necessário, não fora da tela
4. **Given** a planilha do histórico no telefone, **When** há várias colunas, **Then** a pessoa ainda consegue ver data, valores e ações (atalhos e Gerenciar), mesmo que a apresentação se empilhe ou a tabela role só por dentro do cartão

---

### User Story 5 - Documentação do produto e do jeito de rodar (Priority: P3)

Quem chega ao repositório lê no **README** o que o Financia **faz** (telas, regras de negócio visíveis, o que já foi entregue). Como **subir** o sistema fica em **app-settings**. Um **release notes** lista as entregas das features já feitas (gestão financeira, calendário contínuo, sessão/senha esquecida, modais de registro) e esta; todo desenvolvimento seguinte **atualiza esse arquivo** como passo padrão, não como extra opcional.

**Why this priority**: Não muda o uso diário, mas é o pedido explícito de governança e onboarding.

**Independent Test**: Abrir README e não depender dele para senha de banco; abrir app-settings e conseguir o passo a passo de execução; abrir release notes e achar as features 001–004 e esta 005.

**Acceptance Scenarios**:

1. **Given** o README na raiz, **When** uma pessoa de negócio o lê, **Then** entende o propósito do Financia, as telas e o comportamento (histórico, relatórios, login, configurações, perfil) **sem** o roteiro técnico de instalação
2. **Given** `app-settings.md` na raiz, **When** quem vai rodar localmente o segue, **Then** encontra pré-requisitos e comandos que hoje estavam no README (API, aplicativo na tela, banco)
3. **Given** o arquivo de release notes na raiz, **When** alguém consulta o histórico de entregas, **Then** há uma entrada por feature já concluída (001 a 004) em linguagem de produto, mais o que esta feature entrega
4. **Given** uma feature futura passar por especificar → implementar, **When** for dada como feita, **Then** o release notes deve ser atualizado no mesmo ciclo (padrão obrigatório da entrega, não um “se der tempo”)

---

### Edge Cases

- E-mail com espaços no início/fim: tratar como o mesmo e-mail sem os espaços; ainda assim validar o padrão
- Enter no login com o botão já bloqueado: não envia
- Troca de tipo na edição: valor zero ou vazio continua inválido como no registro novo
- Selecionar um item, Editar, e no fundo o Gerenciar ainda listar a seleção: não criar segundo modal de registro por cima de outro de registro; Gerenciar fica atrás até a edição fechar; ao salvar, a lista do dia recarrega e nenhum item fica marcado
- Depois de salvar a edição: se aquele dia não tiver mais item apagável (por exemplo a pessoa mudou a data para outro dia), Gerenciar fecha porque não há lista; senão permanece aberto
- Lançamento recorrente na edição: mesma pergunta de alcance da exclusão (só este / a partir daqui / toda a série); cancelar a pergunta descarta só o passo de gravar, o modal de edição permanece com os dados preenchidos
- Projeção só de leitura no histórico (linha sem identidade apagável): não aparece como item editável em Gerenciar (Gerenciar já lista só o apagável)
- Relatório sem dados: gráfico vazio/zerado, não tela em branco de erro
- Telefone em paisagem: não é o recorte desta feature (retrato dos dois iPhones basta)
- README antigo com senhas ou `taskkill`: isso migra para app-settings; README funcional não replica segredo de banco

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O campo de e-mail no login (e nos modos cadastro e senha esquecida da mesma tela) MUST validar padrão de e-mail e MUST NOT concluir o envio enquanto estiver inválido
- **FR-002**: Com o formulário de login válido, a tecla Enter MUST tentar autenticar como o botão **Entrar**
- **FR-003**: Com exatamente um lançamento selecionado em Gerenciar, o sistema MUST oferecer **Editar**
- **FR-004**: O modal de edição MUST reutilizar a mesma experiência de nova entrada/saída (campos e validações atuais), pré-preenchido com o lançamento escolhido
- **FR-005**: Na edição, a pessoa MUST poder trocar o tipo entrada ↔ saída; data, valor e descrição MUST ser preservados; campos exclusivos do tipo de origem MUST deixar de valer; campos exclusivos do tipo destino começam vazios
- **FR-006**: Sucesso ao editar MUST persistir a mudança (incluindo troca de tipo, que deixa de ser o lançamento antigo e passa a ser o novo tipo), fechar só o modal de edição, atualizar a planilha e a lista de Gerenciar do dia (ainda aberta, sem seleção); se o dia não tiver mais itens apagáveis, Gerenciar MUST fechar
- **FR-006a**: Se o lançamento for recorrente, o salvamento MUST pedir o alcance (só este / a partir daqui / toda a série), iguais à exclusão; cancelar o alcance MUST NOT gravar
- **FR-007**: Fechar o modal de edição sem salvar MUST seguir as mesmas regras de dispensa do modal de nova entrada/saída
- **FR-008**: O relatório gerado MUST exibir gráfico de linhas no período do filtro: saldo corrido (incluindo projeção futura), entradas **do dia** e saídas **do dia** (não acumulado)
- **FR-009**: Login, histórico, relatórios, configurações, perfil e modais MUST ser utilizáveis nas larguras típicas de iPhone 16 e iPhone 16 Pro Max sem scroll horizontal da página
- **FR-010**: O README da raiz MUST documentar o produto (o que o Financia faz); MUST NOT ser o lugar das instruções de execução
- **FR-011**: `app-settings.md` na raiz MUST concentrar como preparar e rodar o sistema localmente (o conteúdo operacional que saiu do README)
- **FR-012**: Um release notes na raiz MUST registrar as entregas das features 001–004 e desta 005, em português voltado a quem usa o produto
- **FR-013**: Entregas futuras MUST atualizar o release notes no mesmo ciclo da implementação (passo padrão, não opcional)

### Key Entities

- **E-mail de acesso**: texto que identifica a conta; precisa parecer e-mail para qualquer envio na tela de entrada
- **Lançamento em edição**: um item já existente (entrada ou saída) escolhido em Gerenciar; pode mudar de tipo ao salvar
- **Série do gráfico de relatório**: três trajetórias no período filtrado — saldo corrido (realizado + projetado), entradas do dia, saídas do dia
- **Release notes**: histórico de entregas do produto na raiz do repositório
- **Documentação funcional**: descrição das capacidades do Financia no README, independente de como se instala

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das tentativas de login com e-mail fora do padrão são recusadas na tela, sem criar sessão
- **SC-002**: Com e-mail e senha válidos, Enter conclui a tentativa de entrada em até 2 segundos de resposta da tela (sucesso ou “credenciais inválidas”), igual ao botão **Entrar**
- **SC-003**: Uma pessoa corrige um lançamento (valor ou tipo) a partir de Gerenciar em menos de 2 minutos, sem apagar e recadastrar
- **SC-004**: Em 100% dos relatórios gerados com sucesso, as três linhas (saldo corrido, entradas do dia, saídas do dia) estão visíveis no período pedido
- **SC-005**: Nos dois tamanhos de iPhone alvo, um revisor percorre login → histórico → um registro ou edição → relatórios sem scroll horizontal da página
- **SC-006**: Um revisor encontra no README o que o produto faz, em `app-settings.md` como rodar, e no release notes as cinco features (001–005), em uma leitura de no máximo 10 minutos

## Assumptions

- “Tecla entre” significa a tecla **Enter** do teclado
- Padrão de e-mail: texto com `@` e domínio (o mesmo critério razoável já usado em cadastro, reforçado de forma visível)
- A troca de tipo na edição **substitui** o lançamento (deixa de ser saída e vira entrada, ou o contrário); não gera os dois ao mesmo tempo
- Após salvar a edição, Gerenciar permanece aberto com a lista do dia atualizada, salvo quando não restar item apagável naquele dia
- Recorrência na edição: reutiliza o diálogo de alcance da exclusão (só este / a partir daqui / toda a série)
- O gráfico de linhas **complementa** totais e categorias já existentes do relatório; não remove CSV nem filtros mês/ano/intervalo; entradas/saídas no gráfico são totais **diários**, o saldo é a linha corrida (pode ter escala visual diferente das outras duas)
- “Saldo mesmo que seja projeção futura” usa a mesma ideia de saldo corrido do histórico no recorte do relatório
- Responsividade cobre o aplicativo inteiro visível à pessoa, não só uma tela; recorte de verificação: iPhone 16 e iPhone 16 Pro Max em retrato
- Release notes e README em **português**; nomes de arquivo na raiz: `README.md`, `app-settings.md`, e um arquivo de notas de versão (por exemplo `RELEASE-NOTES.md`)
- Features 001–004 a resumir no release notes: gestão financeira pessoal; todo dia no histórico e exclusão do dia; sessão obrigatória e senha esquecida; modais de nova entrada/saída
- O padrão “próximos desenvolvimentos preenchem o release notes” vale a partir desta feature: quem implementar 005 já entrega o arquivo preenchido até aqui; features seguintes acrescentam seção no mesmo arquivo ao concluir
