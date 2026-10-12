# Feature Specification: Modais de nova entrada e nova saída no histórico

**Feature Branch**: `004-history-entry-modals`

**Created**: 2026-10-11

**Status**: Draft

**Input**: User description: "Nova entrada e nova saída deve ser um modal sobre a tela de historico. Se selecionar Nova entrada ou Nova saída no topo da página deve considerar a data em branco no modal. Adicionar botão resumido na linha da data para nova entrada e nova saída e a data no modal deve aparecer preenchida com a data da linha."

## Clarifications

### Session 2026-10-11

- Q: Como sair do modal sem salvar? → A: Botão Fechar/Cancelar e a tecla Escape fecham sem salvar; clique no fundo (histórico) não fecha
- Q: Se a data salva não estiver no período visível, o que a planilha faz? → A: Passa a mostrar o período que contém essa data e o lançamento nessa linha

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar pelo topo, com data em branco (Priority: P1)

Na tela de histórico, a pessoa usa **Nova entrada** ou **Nova saída** no topo. O formulário abre **sobre** o histórico (modal), sem empurrar a planilha. O campo de data começa **vazio**; ela escolhe a data, preenche o restante e salva. O histórico permanece visível ao fundo até fechar o modal.

**Why this priority**: É o caminho já conhecido no topo da página; só muda a apresentação. Sem isso, o registro continua ocupando a tela em vez de sobrepor o horizonte.

**Independent Test**: Abrir o histórico, clicar em Nova entrada no topo, ver o modal com data vazia, cancelar; repetir com Nova saída. Salvar uma entrada escolhendo a data no modal e ver o lançamento na linha certa.

**Acceptance Scenarios**:

1. **Given** a pessoa está no histórico autenticado, **When** clica em **Nova entrada** no topo, **Then** um modal de entrada abre sobre a planilha e o campo de data está em branco
2. **Given** a pessoa está no histórico autenticado, **When** clica em **Nova saída** no topo, **Then** um modal de saída abre sobre a planilha e o campo de data está em branco
3. **Given** o modal do topo está aberto com data em branco, **When** tenta salvar sem informar data, **Then** o sistema impede o salvamento e pede a data (mesmo critério já usado no registro)
4. **Given** o modal do topo está aberto, **When** a pessoa usa Fechar/Cancelar ou a tecla Escape, **Then** o histórico não muda e o modal desaparece
4a. **Given** o modal está aberto, **When** a pessoa clica no histórico ao fundo, **Then** o modal permanece aberto e os dados preenchidos não se perdem
5. **Given** o modal do topo está aberto, **When** preenche data e demais campos obrigatórios e salva com sucesso, **Then** o modal fecha e o histórico mostra o novo lançamento na data informada (se essa data for de outro mês/período, a planilha passa a exibir esse período)

---

### User Story 2 - Registrar a partir da linha do dia, com a data preenchida (Priority: P1)

Cada linha de data da planilha tem ações resumidas para nova entrada e nova saída. Ao usá-las, o mesmo tipo de modal abre já com a **data daquela linha** preenchida, para não redigitar o dia.

**Why this priority**: É o ganho novo: lançar no dia que se está olhando, inclusive dias vazios do calendário contínuo.

**Independent Test**: Num dia com movimento e num dia vazio, usar o atalho da linha; o modal deve trazer essa data; salvar e ver o lançamento na mesma linha.

**Acceptance Scenarios**:

1. **Given** a planilha lista os dias do período (com ou sem movimento), **When** a pessoa olha uma linha de data, **Then** vê um atalho resumido de nova entrada e um de nova saída nessa linha
2. **Given** a linha é 07/10, **When** usa o atalho de nova entrada dessa linha, **Then** o modal de entrada abre sobre o histórico com a data 07/10 já preenchida
3. **Given** a linha é 07/10, **When** usa o atalho de nova saída dessa linha, **Then** o modal de saída abre sobre o histórico com a data 07/10 já preenchida
4. **Given** o modal veio da linha com a data preenchida, **When** a pessoa altera a data antes de salvar, **Then** o lançamento segue a data que ela deixou no campo (não fica preso à linha original); se a nova data for de outro período visível, a planilha muda para esse período
5. **Given** o modal veio da linha, **When** salva com sucesso na mesma data da linha, **Then** o modal fecha e o histórico atualiza; o lançamento aparece no dia da data salva

---

### Edge Cases

- Só um modal de registro por vez: abrir entrada fecha saída (e o contrário); não empilhar dois formulários
- Os atalhos da linha existem também em dia sem movimento (linha vazia do calendário)
- **Gerenciar** (exclusão em lote) permanece na linha quando houver itens apagáveis; não substitui os atalhos de nova entrada/saída
- Recorrência, categoria, fonte de pagamento e demais campos do registro atual continuam no modal; esta feature não remove regras de negócio
- Com o modal aberto, cliques no fundo (histórico) não fecham o modal nem descartam o formulário
- Fechar/Cancelar e a tecla Escape saem sem salvar
- Falha ao salvar (validação ou erro do sistema): o modal permanece aberto com a mensagem; a data preenchida (ou em branco) não é apagada à toa
- Data salva fora do mês/período que está na tela: após sucesso, a planilha passa a esse período e destaca o lançamento na linha da data (mesmo recorte de mês já usado nas setas do histórico)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: **Nova entrada** e **Nova saída** no topo do histórico MUST abrir um modal sobre a tela de histórico, não um bloco que empurra a planilha para baixo
- **FR-002**: No modal aberto pelo topo, o campo de data MUST iniciar em branco
- **FR-003**: Cada linha de data da planilha MUST oferecer atalho resumido para nova entrada e para nova saída
- **FR-004**: No modal aberto pelo atalho da linha, o campo de data MUST iniciar preenchido com a data daquela linha
- **FR-005**: Os formulários no modal MUST permitir o mesmo registro de entrada e de saída já existente (campos e validações atuais, inclusive data obrigatória para salvar)
- **FR-006**: Sucesso ao salvar MUST fechar o modal, atualizar saldo e totais, e garantir que a planilha visível seja o período (mês) que contém a data salva, com o lançamento nessa linha
- **FR-007**: Fechar/Cancelar e a tecla Escape MUST fechar o modal sem criar lançamento; clique no fundo MUST NOT fechar o modal nem descartar o que já foi preenchido
- **FR-008**: Não MUST haver dois modais de registro abertos ao mesmo tempo
- **FR-009**: Relatórios, configurações, perfil e o diálogo de gerenciar/apagar do dia estão fora do escopo desta alteração, salvo coexistir com os atalhos na mesma linha

### Key Entities

- **Modal de registro**: sobreposição sobre o histórico para criar uma entrada ou uma saída; leva origem (topo vs linha) que define se a data começa vazia ou preenchida
- **Linha de data**: um dia civil já exibido no histórico; origem do atalho resumido e da data sugerida
- **Lançamento**: entrada ou saída criada; inalterado nas regras de saldo, recorrência e cartão

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% dos cliques em Nova entrada ou Nova saída no topo, o modal cobre o histórico e a data está vazia na abertura
- **SC-002**: Em 100% dos cliques no atalho da linha, o modal abre com a data exatamente igual à daquela linha
- **SC-003**: Uma pessoa registra um lançamento a partir da linha (incluindo dia vazio) em menos de 1 minuto, sem redigitar a data se não quiser mudá-la
- **SC-004**: Após salvar com sucesso, em até 2 segundos o modal já não está aberto e a pessoa vê a linha da data salva (trocando de mês/período se precisar) com o novo lançamento
- **SC-005**: Fechar/Cancelar ou Escape 100% das vezes deixa a planilha como estava, sem lançamento novo; clique no fundo 100% das vezes mantém o modal aberto

## Assumptions

- A pessoa já está autenticada na tela de histórico (sessão da 003)
- Os campos e regras de nova entrada/saída (valor, recorrência, categoria, fonte de pagamento, fatura de cartão etc.) permanecem os de hoje; muda só o invólucro (modal) e a origem da data
- “Botão resumido” significa ação compacta na linha (rótulo curto ou equivalente óbvio), distinta dos botões largos do topo
- Toda linha de data do período visível recebe os atalhos, inclusive dias sem movimento
- A pessoa pode editar a data no modal mesmo quando ela veio preenchida da linha
- Relatório e CSV não passam a criar lançamentos por esta tela
