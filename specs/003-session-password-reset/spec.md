# Feature Specification: Sessão obrigatória e troca de senha esquecida

**Feature Branch**: `003-session-password-reset`

**Created**: 2026-10-11

**Status**: Draft

**Input**: User description: "Novas features devem ser implementadas. Todas as rotas do frontend deve validar se há um usuário logado com sessão aberta, caso contrário, deve direcionar para a rota \login. Adicionar um botão de trocar senha para caso o usuário exista na base mas não se recorde da senha"

## Clarifications

### Session 2026-10-11

- Q: Depois da troca de senha esquecida, a pessoa entra automaticamente ou precisa fazer login? → A: Volta ao login e deve entrar com e-mail e a senha nova (sem sessão automática)
- Q: Sessões já abertas da conta devem deixar de valer na hora da troca esquecida? → A: Sim — encerrar todas as sessões abertas daquela conta no momento da troca
- Q: A senha nova precisa ser digitada duas vezes? → A: Sim — senha nova e confirmação; se forem diferentes, não grava e mostra erro

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Área autenticada só com sessão aberta (Priority: P1)

Quem tenta abrir qualquer tela da aplicação financeira (histórico, relatórios, configurações, perfil ou um endereço inválido) sem estar logado, ou com a sessão encerrada, é levado imediatamente para a tela de login. Só continua na área autenticada quem tem sessão válida.

**Why this priority**: Sem essa barreira, dados financeiros ficam acessíveis a quem abrir o endereço da aplicação. É o pré-requisito de qualquer outra função.

**Independent Test**: Abrir, sem estar logado, o histórico, relatórios, configurações, perfil e um endereço inexistente; em todos os casos o usuário deve ver apenas a tela de login. Depois de entrar com credenciais válidas, as mesmas telas devem abrir normalmente.

**Acceptance Scenarios**:

1. **Given** não há sessão aberta, **When** a pessoa tenta abrir histórico, relatórios, configurações ou perfil (diretamente ou por atalho), **Then** é redirecionada para `/login` e não vê dados financeiros
2. **Given** não há sessão aberta, **When** a pessoa abre um endereço que não existe na aplicação, **Then** também é redirecionada para `/login` (não cai numa tela autenticada vazia)
3. **Given** a sessão estava aberta e deixou de ser válida (expirou ou foi encerrada), **When** a pessoa tenta continuar numa tela autenticada, **Then** é redirecionada para `/login`
4. **Given** a pessoa tem sessão válida, **When** navega entre as telas autenticadas, **Then** permanece nelas sem ser mandada ao login
5. **Given** a pessoa não está logada, **When** abre `/login` (ou o fluxo de criar conta / trocar senha esquecida), **Then** essas telas permanecem acessíveis — não há redirecionamento em loop

---

### User Story 2 - Trocar senha quando a pessoa não se lembra da atual (Priority: P1)

Na tela de login, a pessoa que já tem cadastro mas esqueceu a senha usa um botão **Trocar senha**, informa o e-mail da conta e a senha nova **duas vezes** (confirmação), sem precisar da senha antiga. O sistema **não** abre sessão: a pessoa volta ao login e entra com e-mail e a senha nova.

**Why this priority**: Sem isso, uma conta existente fica inutilizável se a senha for esquecida; o botão de alterar senha no perfil exige a senha atual e não resolve esse caso.

**Independent Test**: Com uma conta já cadastrada, sair (ou não logar), acionar Trocar senha, concluir o fluxo e entrar com a senha nova; a senha antiga deixa de funcionar.

**Acceptance Scenarios**:

1. **Given** a pessoa está na tela de login, **When** olha o formulário, **Then** vê um botão **Trocar senha** visível sem precisar já estar autenticada
2. **Given** existe uma conta com aquele e-mail, **When** a pessoa informa esse e-mail e a mesma senha nova nos dois campos (sem senha atual, código ou link), **Then** a senha nova passa a ser a única aceita no login, **e** a pessoa vê de novo a tela de login (não entra automaticamente na área autenticada)
2a. **Given** a pessoa preenche e-mail e senha nova, **When** a confirmação é diferente da senha nova, **Then** o sistema mostra erro, não grava a senha e a senha anterior continua válida
3. **Given** a troca esquecida acabou de ser aceita, **When** a pessoa ainda não digitou e-mail e senha nova no login, **Then** tentativas de abrir histórico ou outras telas autenticadas redirecionam para `/login`
4. **Given** a pessoa informa um e-mail que não está cadastrado, **When** tenta trocar a senha, **Then** o sistema não confirma que o e-mail “não existe” de forma explícita (mensagem genérica) e nenhuma senha de outra conta é alterada
5. **Given** a pessoa está logada e lembra a senha atual, **When** usa a troca de senha já existente no perfil, **Then** esse fluxo continua exigindo a senha atual (esta história não o remove)
6. **Given** a mesma conta ainda tem sessão aberta em outro aparelho ou aba, **When** a troca esquecida é concluída, **Then** essa sessão deixa de valer e a próxima ação na área autenticada vai para `/login`

---

### Edge Cases

- Sessão inválida no meio de uma ação (ex.: salvar um lançamento): a pessoa é enviada ao login e os dados financeiros deixam de ser exibidos
- Pessoa já autenticada que abre `/login`: pode permanecer no login ou ser levada à área autenticada; o default é levá-la ao histórico para não parecer deslogada
- Senha nova igual à antiga: aceita (não é necessário recusar)
- Senha nova abaixo do mínimo já usado no cadastro: recusa com mensagem clara e não altera a senha
- Confirmação diferente da senha nova: recusa com mensagem clara, não altera a senha e não encerra sessões
- Dois pedidos de troca para o mesmo e-mail em sequência: vale a última senha definida com sucesso
- Troca esquecida bem-sucedida não cria sessão; a área autenticada só abre após login com a senha nova
- Troca esquecida bem-sucedida encerra todas as sessões já abertas daquela conta (outras abas e aparelhos passam a ser tratados como sessão inválida)
- Recarregar a página numa rota autenticada com sessão válida: continua autenticada; sem sessão, volta ao login

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Toda tela autenticada da aplicação (incluindo histórico, relatórios, configurações, perfil, raiz da área logada e endereços inexistentes) MUST verificar se há usuário com sessão aberta antes de exibir conteúdo
- **FR-002**: Se não houver sessão aberta ou a sessão não for mais válida, o sistema MUST redirecionar para `/login` e MUST NOT mostrar dados financeiros
- **FR-003**: As telas de login, criação de conta e troca de senha esquecida MUST permanecer acessíveis sem sessão, para não criar redirecionamento em círculo
- **FR-004**: A tela de login MUST exibir um botão **Trocar senha** destinado a quem já tem cadastro e não se lembra da senha
- **FR-005**: A troca de senha esquecida MUST exigir o e-mail da conta, a senha nova e a confirmação da senha nova (mesmas regras mínimas do cadastro); se senha e confirmação diferirem, o sistema MUST recusar, informar o erro e MUST NOT alterar a senha nem as sessões
- **FR-006**: A troca de senha esquecida MUST concluir apenas para e-mail que já existe na base; a senha armazenada dessa conta passa a ser a nova
- **FR-007**: A troca de senha esquecida MUST aceitar como comprovação apenas o e-mail já cadastrado mais a senha nova — sem senha atual, código, link ou dado extra
- **FR-008**: Tentativas com e-mail inexistente MUST devolver mensagem genérica (sem revelar se o e-mail está cadastrado) e MUST NOT alterar outras contas
- **FR-009**: Após troca esquecida bem-sucedida, o sistema MUST devolver a pessoa à tela de login **sem** abrir sessão; ela MUST autenticar com e-mail e a senha nova; a senha anterior MUST deixar de autenticar
- **FR-010**: A alteração de senha no perfil (pessoa já logada, com senha atual) permanece fora desta alteração, salvo o redirecionamento de sessão de FR-001/FR-002
- **FR-011**: No instante em que a troca esquecida é aceita, o sistema MUST invalidar **todas** as sessões abertas daquela conta; qualquer uso posterior da área autenticada MUST seguir FR-002

### Key Entities

- **Sessão do usuário**: indicação de que a pessoa está autenticada e autorizada a ver a área financeira; quando ausente, inválida ou invalidada por troca de senha esquecida, o destino é o login
- **Conta**: cadastro existente identificado pelo e-mail, com senha que pode ser substituída pelo fluxo de esquecimento ou pelo fluxo autenticado do perfil
- **Troca de senha esquecida**: pedido iniciado na tela de login, sem sessão, para redefinir a senha de uma conta existente

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% das tentativas de abrir uma tela autenticada sem sessão (incluindo endereço inexistente), o usuário chega em `/login` em até 2 segundos e não vê saldo, lançamentos nem relatórios
- **SC-002**: Um usuário com sessão válida completa a navegação entre as telas autenticadas existentes sem ser deslogado indevidamente
- **SC-003**: Um titular que esqueceu a senha conclui a troca (do botão na tela de login até a nova senha aceita) em menos de 2 minutos, sem precisar da senha antiga
- **SC-004**: Depois da troca, a pessoa está na tela de login (não na área autenticada); o login com a senha nova funciona na primeira tentativa; o login com a senha antiga falha
- **SC-005**: Quem conhece o e-mail cadastrado conclui a troca esquecida sem a senha antiga; quem informa e-mail inexistente recebe a mesma mensagem genérica e não descobre se a conta existe
- **SC-006**: Em 100% das contas com sessão ainda aberta no momento da troca esquecida, a próxima tentativa de usar a área autenticada nessa sessão cai em `/login` em até 2 segundos, sem mostrar dados financeiros

## Assumptions

- Login e cadastro já existem; esta feature reforça o bloqueio de sessão em todas as telas autenticadas e acrescenta troca de senha para quem esqueceu
- Endereços públicos desta feature: `/login` e o fluxo de troca de senha esquecida (e o cadastro já oferecido na mesma tela de login)
- Quem já está autenticado e lembra a senha continua usando a troca no perfil, que pede a senha atual
- Mensagem genérica em e-mail inexistente reduz enumeração de contas
- A senha nova segue o mesmo mínimo de caracteres já exigido no cadastro
- Não há vários papéis de usuário: qualquer conta autenticada acessa as mesmas telas financeiras
- Comprovação da troca esquecida (escolha explícita): e-mail cadastrado + senha nova, sem prova extra. Quem souber o e-mail pode redefinir a senha; o produto prioriza recuperação simples nesta versão
- Não há envio de e-mail transacional neste fluxo
