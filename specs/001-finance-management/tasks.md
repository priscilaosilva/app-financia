# Tasks: Sistema de Gestão Financeira

**Input**: Design documents from `specs/001-finance-management/`

**Prerequisites**: [plan.md](./plan.md) · [spec.md](./spec.md) · [research.md](./research.md) · [data-model.md](./data-model.md) · [contracts/api-contract.md](./contracts/api-contract.md) · [quickstart.md](./quickstart.md)

**Stack**: Java 17 + Spring Boot 3.x (backend) · Angular 17 (frontend) · MySQL 8.x

**Total tasks**: 85 | **Parallelizable**: 38 marcadas com [P]

---

## Format: `[ID] [P?] [Story?] Description — file/path`

- **[P]**: Pode executar em paralelo (arquivos diferentes, sem dependências incompletas)
- **[US#]**: User Story da spec.md (US1–US5)
- Sem label de story = Setup ou Foundational

---

## Phase 1: Setup (Estrutura do Projeto)

**Purpose**: Inicialização dos projetos backend e frontend com dependências e configuração base.

- [x] T001 Criar projeto Spring Boot 3.x com Java 17 via Spring Initializr com dependências: Web, Data JPA, Security, Validation, MySQL Driver, Lombok, Flyway — `backend/pom.xml`
- [x] T002 Criar projeto Angular 17 via Angular CLI com roteamento e Angular Material — `frontend/`
- [x] T003 [P] Configurar `application.yml` com datasource MySQL (url, username, password via env vars), Flyway, JWT secret (env var), CORS origins — `backend/src/main/resources/application.yml`
- [x] T004 [P] Configurar `environment.ts` e `environment.prod.ts` com `apiUrl`; adicionar proxy.conf.json para redirecionar `/api/*` ao backend local — `frontend/src/environments/`
- [x] T005 [P] Criar estrutura de diretórios do projeto Flyway para migrações SQL — `backend/src/main/resources/db/migration/`

---

## Phase 2: Foundational (Pré-requisitos Bloqueantes)

**Purpose**: Autenticação JWT, User entity e infraestrutura base que TODAS as User Stories dependem.

**⚠️ CRÍTICO**: Nenhuma User Story pode iniciar antes desta fase estar completa.

- [x] T006 Criar migration `V1__create_users.sql` com tabela `users`: campos `id BIGINT AUTO_INCREMENT PK`, `name VARCHAR(100) NOT NULL`, `email VARCHAR(255) NOT NULL UNIQUE`, `password_hash VARCHAR(255) NOT NULL`, `theme_preference ENUM('LIGHT','DARK') NOT NULL DEFAULT 'LIGHT'`, `created_at DATETIME NOT NULL DEFAULT NOW()`, `updated_at DATETIME NOT NULL` — `backend/src/main/resources/db/migration/V1__create_users.sql`
- [x] T007 Criar entidade JPA `User` com campos mapeados da tabela users; anotar com `@Entity`, `@Table(name="users")`; incluir `@Column(unique=true)` em email — `backend/src/main/java/com/financia/user/User.java`
- [x] T008 [P] Criar `UserRepository` extendendo `JpaRepository<User, Long>` com método `findByEmail(String email): Optional<User>` — `backend/src/main/java/com/financia/user/UserRepository.java`
- [x] T009 Implementar `JwtTokenProvider`: método `generateToken(String email, Long userId)` assinando com HMAC-SHA256 e expiração configurável; método `validateToken(String token): boolean`; método `getEmailFromToken(String token): String` — `backend/src/main/java/com/financia/auth/JwtTokenProvider.java`
- [x] T010 [P] Implementar `JwtAuthenticationFilter` extendendo `OncePerRequestFilter`: extrair Bearer token do header, validar via JwtTokenProvider, setar `SecurityContextHolder` — `backend/src/main/java/com/financia/config/JwtAuthenticationFilter.java`
- [x] T011 Implementar `SecurityConfig` com `@EnableWebSecurity`: liberar `/api/auth/**` sem autenticação; exigir autenticação para todo o resto; adicionar JwtAuthenticationFilter antes de UsernamePasswordAuthenticationFilter; configurar CORS para `localhost:4200` — `backend/src/main/java/com/financia/config/SecurityConfig.java`
- [x] T012 Implementar `AuthService`: método `register(name, email, password)` — validar email único (lançar 409 se duplicado), salvar com `BCryptPasswordEncoder`; método `login(email, password)` — verificar senha com BCrypt, retornar JWT — `backend/src/main/java/com/financia/auth/AuthService.java`
- [x] T013 Implementar `AuthController` com `POST /api/auth/register` (retorna 201 com {id, name, email}) e `POST /api/auth/login` (retorna 200 com {token, expires_at}); lançar `409` se email duplicado, `401` se credenciais inválidas — `backend/src/main/java/com/financia/auth/AuthController.java`
- [x] T014 [P] Implementar `GlobalExceptionHandler` com `@RestControllerAdvice` tratando: `EntityNotFoundException` → 404, `DuplicateEmailException` → 409, `MethodArgumentNotValidException` → 422, `AccessDeniedException` → 403, `Exception` genérica → 500; formato de resposta `{error, field?}` — `backend/src/main/java/com/financia/config/GlobalExceptionHandler.java`
- [x] T015 [P] Criar `LoginComponent` com formulário (email, password), chamar `AuthService.login()`, armazenar token no `localStorage`, redirecionar para `/history` — `frontend/src/app/auth/login/login.component.ts + .html`
- [x] T016 Implementar Angular `AuthService`: `login(email, password)`, `logout()` (limpar localStorage), `isAuthenticated(): boolean`, `getToken(): string`, `getCurrentUser()` — `frontend/src/app/auth/auth.service.ts`
- [x] T017 [P] Implementar `JwtInterceptor` (HTTP interceptor): adicionar header `Authorization: Bearer {token}` em todas as requisições; interceptar 401 e redirecionar para `/login` — `frontend/src/app/shared/interceptors/jwt.interceptor.ts`
- [x] T018 [P] Implementar `AuthGuard` usando `CanActivateFn`: redirecionar para `/login` se `AuthService.isAuthenticated()` for false — `frontend/src/app/auth/auth.guard.ts`
- [x] T019 Criar `AppLayoutComponent` com menu lateral fixo contendo itens: **Histórico**, **Relatórios**, **Configurações**, **Perfil**; incluir `<router-outlet>` para conteúdo principal — `frontend/src/app/layout/app-layout.component.ts + .html`
- [x] T020 Configurar `app.routes.ts` com lazy loading: rota `/login` → `AuthModule` (pública); rotas `/history`, `/reports`, `/settings`, `/profile` → módulos correspondentes protegidos por `AuthGuard`; rota padrão `''` redireciona para `/history` — `frontend/src/app/app.routes.ts`

**Checkpoint**: Autenticação funcional — usuario consegue registrar, fazer login, receber JWT e acessar rotas protegidas.

---

## Phase 3: User Story 1 — Registro e Visualização de Saldo (P1) 🎯 MVP

**Goal**: Usuário vê saldo total atualizado e histórico financeiro em planilha com colunas Entrada/Saída/Saldo por data, podendo navegar entre períodos.

**Independent Test**: Quickstart Cenário 1 — registrar 1 entrada e 1 saída via API e verificar que `/history` retorna linhas com saldo sequencial correto e `/balance` retorna o valor correto.

### Backend — Entidades e Motor de Histórico

- [x] T021 Criar migration `V2__create_income_entries.sql`: tabela `income_entries` com campos `id BIGINT AUTO_INCREMENT PK`, `user_id BIGINT NOT NULL FK→users`, `date DATE NOT NULL`, `amount DECIMAL(15,2) NOT NULL CHECK(amount > 0)`, `income_source_id BIGINT NULL FK→income_sources`, `source_name_snapshot VARCHAR(100)`, `description TEXT`, `recurrence_rule_id BIGINT NULL FK→recurrence_rules`, `is_base_recurrence BOOLEAN NOT NULL DEFAULT FALSE`, `created_at DATETIME NOT NULL`; índice `idx_income_user_date(user_id, date)` — `backend/src/main/resources/db/migration/V2__create_income_entries.sql`
- [x] T022 Criar migration `V3__create_expense_entries.sql`: tabela `expense_entries` com campos `id BIGINT AUTO_INCREMENT PK`, `user_id BIGINT NOT NULL FK→users`, `date DATE NOT NULL`, `amount DECIMAL(15,2) NOT NULL CHECK(amount > 0)`, `expense_category_id BIGINT NULL FK→expense_categories`, `category_name_snapshot VARCHAR(100)`, `payment_method_id BIGINT NULL FK→payment_methods`, `payment_method_name_snapshot VARCHAR(100)`, `payment_status ENUM('CONFIRMED','PENDING_CREDIT') NOT NULL DEFAULT 'CONFIRMED'`, `description TEXT`, `recurrence_rule_id BIGINT NULL FK→recurrence_rules`, `is_base_recurrence BOOLEAN NOT NULL DEFAULT FALSE`, `created_at DATETIME NOT NULL`; índices `idx_expense_user_date(user_id, date)` e `idx_expense_status(user_id, payment_status, date)` — `backend/src/main/resources/db/migration/V3__create_expense_entries.sql`
- [x] T023 [P] Criar entidade JPA `IncomeEntry` com todos os campos mapeados; relacionamento `@ManyToOne(optional=true)` para `IncomeSource` e `RecurrenceRule` — `backend/src/main/java/com/financia/income/IncomeEntry.java`
- [x] T024 [P] Criar entidade JPA `ExpenseEntry` com todos os campos; campo `paymentStatus` como `@Enumerated(EnumType.STRING)` com values `CONFIRMED` e `PENDING_CREDIT`; relacionamentos `@ManyToOne(optional=true)` para `ExpenseCategory`, `PaymentMethod`, `RecurrenceRule` — `backend/src/main/java/com/financia/expense/ExpenseEntry.java`
- [x] T025 [P] Criar `IncomeEntryRepository` e `ExpenseEntryRepository` com queries: `findByUserIdAndDateBetweenOrderByDateAsc`, `sumAmountByUserIdAndDateLessThanEqual`, `findPendingCreditByUserIdAndPaymentMethodIdAndDateBetween` — `backend/src/main/java/com/financia/income/IncomeEntryRepository.java` e `backend/src/main/java/com/financia/expense/ExpenseEntryRepository.java`
- [x] T026 [US1] Implementar `HistoryService`: (a) buscar todas as income_entries e expense_entries do usuário no período; (b) calcular `running_balance` acumulado dinamicamente — para cada data em ordem cronológica somar income.amount e subtrair expense.amount WHERE payment_status='CONFIRMED'; (c) para cada payment_method do tipo CREDIT_CARD ativo, verificar se há due_date no período e injetar linha sintética `CREDIT_CARD_BILL` somando todos os expenses PENDING_CREDIT do billing period correspondente; (d) retornar lista de `HistoryRowDTO` por data — `backend/src/main/java/com/financia/history/HistoryService.java`
- [x] T027 [US1] Implementar `GET /api/balance`: retornar `{balance, calculated_at}` onde balance = SUM(income_entries.amount WHERE user_id=? AND date <= TODAY) - SUM(expense_entries.amount WHERE user_id=? AND payment_status='CONFIRMED' AND date <= TODAY) — `backend/src/main/java/com/financia/history/BalanceController.java`
- [x] T028 [US1] Implementar `GET /api/history?from=&to=`: validar que from e to estão presentes (422 se não); chamar HistoryService; retornar `HistoryResponseDTO` com array de rows; cada row contém: date, entries[], daily_income, daily_expense_confirmed, daily_expense_pending, running_balance — `backend/src/main/java/com/financia/history/HistoryController.java`

### Frontend — Tela de Histórico

- [x] T029 [P] [US1] Criar `HistoryService` Angular: `getHistory(from: string, to: string): Observable<HistoryResponse>`; `getBalance(): Observable<BalanceResponse>` — `frontend/src/app/history/history.service.ts`
- [x] T030 [US1] Implementar `HistoryComponent`: carregar histórico do mês atual ao iniciar; renderizar tabela com colunas Data/Entrada/Saída/Saldo; exibir entradas PENDING_CREDIT com fundo diferenciado (ex: cor amarela ou ícone de cartão); exibir linha CREDIT_CARD_BILL com rótulo "Fatura [nome]"; mostrar saldo total no topo atualizado via GET /balance — `frontend/src/app/history/history.component.ts + .html`
- [x] T031 [US1] Adicionar navegação de período ao `HistoryComponent`: botão "←" (mês anterior) e "→" (próximo mês); ao navegar para datas futuras, linhas com `is_recurring_projection=true` exibidas em itálico com ícone de recorrência — `frontend/src/app/history/history.component.ts`

**Checkpoint (US1)**: GET /balance e GET /history funcionais; tela exibe planilha com saldo acumulado; navegação entre meses funcional. *(Dados visíveis após US2 e US3 fornecerem entradas reais.)*

---

## Phase 4: User Story 2 — Registro de Entradas de Dinheiro (P1)

**Goal**: Usuário registra entradas com data (obrigatório), valor (obrigatório), fonte de receita (opcional) e descrição; gerencia fontes de receita customizadas.

**Independent Test**: Quickstart Cenário 1 passos 2–4 — POST /income com data e valor → verificar no GET /history que a entrada aparece na data correta e GET /balance retorna valor atualizado.

### Backend — Fontes de Receita e Entradas

- [x] T032 Criar migration `V4__create_income_sources.sql`: tabela `income_sources` com `id BIGINT AUTO_INCREMENT PK`, `user_id BIGINT NOT NULL FK→users`, `name VARCHAR(100) NOT NULL`, `active BOOLEAN NOT NULL DEFAULT TRUE`, `created_at DATETIME NOT NULL`; índice `idx_income_sources_user(user_id, active)` — `backend/src/main/resources/db/migration/V4__create_income_sources.sql`
- [x] T033 [P] Criar entidade JPA `IncomeSource` com campos mapeados; `active` controla soft delete — `backend/src/main/java/com/financia/income/IncomeSource.java`
- [x] T034 [P] Criar `IncomeSourceRepository` com `findByUserIdAndActiveTrue(Long userId): List<IncomeSource>` — `backend/src/main/java/com/financia/income/IncomeSourceRepository.java`
- [x] T035 Implementar `IncomeSourceService`: `listActive(userId)`, `create(userId, name)`, `rename(userId, id, name)`, `softDelete(userId, id)` — soft delete define `active=FALSE` sem deletar o registro (FR-022) — `backend/src/main/java/com/financia/income/IncomeSourceService.java`
- [x] T036 Implementar `GET/POST/PUT/DELETE /api/income-sources` em `IncomeSourceController`; DELETE retorna 204 após soft delete; GET retorna apenas sources com `active=TRUE` — `backend/src/main/java/com/financia/income/IncomeSourceController.java`
- [x] T037 Implementar `IncomeService`: `createIncome(userId, date NOT NULL, amount NOT NULL >0, sourceId?, description?, recurrence?)` — validar campos obrigatórios (422 se ausentes); buscar IncomeSource e gravar `source_name_snapshot=source.name` se sourceId informado; persistir IncomeEntry com `is_base_recurrence=false` se sem recorrência — `backend/src/main/java/com/financia/income/IncomeService.java`
- [x] T038 Implementar `POST /api/income` (201) e `PUT /api/income/{id}` (200) e `DELETE /api/income/{id}` (204) em `IncomeController`; PUT e DELETE retornam 404 se id não pertencer ao usuário autenticado — `backend/src/main/java/com/financia/income/IncomeController.java`

### Frontend — Formulário de Entrada

- [x] T039 [P] [US2] Criar `IncomeFormComponent` com campos: date (required, date picker), amount (required, número positivo), income_source (select populado via GET /income-sources), description (textarea); botão submit desabilitado enquanto form inválido; exibir mensagem de erro por campo — `frontend/src/app/income/income-form/income-form.component.ts + .html`
- [x] T040 [P] [US2] Implementar Angular `IncomeService`: `createIncome(payload)`, `updateIncome(id, payload)`, `deleteIncome(id)`, `getIncomeSources()`, `createIncomeSource(name)`, `deleteIncomeSource(id)` — `frontend/src/app/income/income.service.ts`
- [x] T041 [US2] Criar tela de gestão de Fontes de Receita em Configurações: listar fontes ativas, formulário de criação, botão renomear, botão excluir com confirmação — `frontend/src/app/settings/income-sources/income-sources.component.ts + .html`
- [x] T042 [US2] Após submit bem-sucedido do income form, disparar refresh de `GET /balance` e `GET /history` para atualizar a tela de histórico em tempo real — `frontend/src/app/income/income-form/income-form.component.ts`

**Checkpoint (US2)**: Usuário cadastra fonte de receita e registra entrada; histórico exibe a entrada na data correta; saldo é atualizado.

---

## Phase 5: User Story 3 — Saídas com Categorias e Cartão de Crédito (P1)

**Goal**: Usuário cadastra categorias, fontes de pagamento (incluindo cartão de crédito com período e vencimento) e registra saídas. Gastos de cartão aparecem como pendentes e são debitados no vencimento.

**Independent Test**: Quickstart Cenários 1 (passo 3) e 3 — POST /expense com conta corrente → saldo reduzido; POST /expense com cartão → status PENDING_CREDIT; no dia de vencimento linha sintética "Fatura" aparece no histórico.

### Backend — Categorias, Fontes de Pagamento e Saídas

- [x] T043 Criar migration `V5__create_expense_categories.sql`: tabela `expense_categories` com `id BIGINT AUTO_INCREMENT PK`, `user_id BIGINT NOT NULL FK→users`, `name VARCHAR(100) NOT NULL`, `active BOOLEAN NOT NULL DEFAULT TRUE`, `created_at DATETIME NOT NULL`; índice `idx_categories_user(user_id, active)` — `backend/src/main/resources/db/migration/V5__create_expense_categories.sql`
- [x] T044 Criar migration `V6__create_payment_methods.sql`: tabela `payment_methods` com `id BIGINT AUTO_INCREMENT PK`, `user_id BIGINT NOT NULL FK→users`, `name VARCHAR(100) NOT NULL`, `type ENUM('CHECKING','SAVINGS','CREDIT_CARD') NOT NULL`, `due_day TINYINT NULL`, `billing_start_day TINYINT NULL`, `billing_end_day TINYINT NULL`, `active BOOLEAN NOT NULL DEFAULT TRUE`, `created_at DATETIME NOT NULL`; índice `idx_payment_methods_user(user_id, active)` — `backend/src/main/resources/db/migration/V6__create_payment_methods.sql`
- [x] T045 [P] Criar entidade JPA `ExpenseCategory` — `backend/src/main/java/com/financia/expense/ExpenseCategory.java`
- [x] T046 [P] Criar entidade JPA `PaymentMethod` com campo `type` como `@Enumerated(EnumType.STRING)` e campos `dueDay`, `billingStartDay`, `billingEndDay` nullable — `backend/src/main/java/com/financia/paymentmethod/PaymentMethod.java`
- [x] T047 [P] Implementar `ExpenseCategoryService`: `listActive`, `create`, `rename`, `softDelete` (active=FALSE, FR-022) — `backend/src/main/java/com/financia/expense/ExpenseCategoryService.java`
- [x] T048 [P] Implementar `PaymentMethodService`: `create` com validação — se `type=CREDIT_CARD` então `due_day`, `billing_start_day` e `billing_end_day` são obrigatórios (lançar 422 se ausentes, FR-006); `listActive`, `rename`, `softDelete` — `backend/src/main/java/com/financia/paymentmethod/PaymentMethodService.java`
- [x] T049 [P] Implementar `GET/POST/PUT/DELETE /api/categories` em `ExpenseCategoryController` — `backend/src/main/java/com/financia/expense/ExpenseCategoryController.java`
- [x] T050 [P] Implementar `GET/POST/PUT/DELETE /api/payment-methods` em `PaymentMethodController`; POST valida obrigatoriedade de due_day/billing_start_day/billing_end_day quando type=CREDIT_CARD — `backend/src/main/java/com/financia/paymentmethod/PaymentMethodController.java`
- [x] T051 Implementar `ExpenseService`: `createExpense(userId, date NOT NULL, amount NOT NULL >0, categoryId?, paymentMethodId?, description?)` — validar campos obrigatórios (422); se paymentMethodId informado buscar PaymentMethod; se `paymentMethod.type=CREDIT_CARD` setar `payment_status=PENDING_CREDIT` automaticamente, caso contrário `CONFIRMED`; gravar `category_name_snapshot` e `payment_method_name_snapshot` no momento da criação (FR-022) — `backend/src/main/java/com/financia/expense/ExpenseService.java`
- [x] T052 Implementar `POST /api/expense` (201), `PUT /api/expense/{id}` (200), `DELETE /api/expense/{id}` (204) em `ExpenseController` — `backend/src/main/java/com/financia/expense/ExpenseController.java`

### Frontend — Formulário de Saída e Configurações

- [x] T053 [P] [US3] Criar `ExpenseFormComponent` com campos: date (required), amount (required), category (select via GET /categories), payment_method (select via GET /payment-methods), description; mostrar ícone de cartão de crédito quando payment_method do tipo CREDIT_CARD for selecionado — `frontend/src/app/expense/expense-form/expense-form.component.ts + .html`
- [x] T054 [P] [US3] Implementar Angular `ExpenseService`: `createExpense`, `updateExpense`, `deleteExpense`, `getCategories`, `createCategory`, `deleteCategory`, `getPaymentMethods`, `createPaymentMethod`, `deletePaymentMethod` — `frontend/src/app/expense/expense.service.ts`
- [x] T055 [US3] Criar tela de gestão de Categorias de Gasto em Configurações — `frontend/src/app/settings/expense-categories/expense-categories.component.ts + .html`
- [x] T056 [US3] Criar tela de gestão de Fontes de Pagamento em Configurações: formulário com campo `type` (select: Conta Corrente / Reserva / Cartão de Crédito); exibir campos `due_day`, `billing_start_day`, `billing_end_day` condicionalmente apenas quando type=CREDIT_CARD for selecionado (FR-006) — `frontend/src/app/settings/payment-methods/payment-methods.component.ts + .html`
- [x] T057 [US3] Após submit do expense form, disparar refresh de balance e history; verificar que entrada PENDING_CREDIT aparece com estilo visual diferenciado (fundo amarelo ou ícone de cartão) no histórico — `frontend/src/app/expense/expense-form/expense-form.component.ts`

**Checkpoint (US3)**: Saídas de conta corrente debitam saldo imediatamente; saídas de cartão aparecem como "pendente" sem afetar saldo; linha de fatura sintética aparece no dia de vencimento.

---

## Phase 6: User Story 4 — Transações Recorrentes (P2)

**Goal**: Entradas e saídas podem ser configuradas como recorrentes (diária, mensal, anual). O histórico projeta ocorrências futuras. Editar/cancelar uma ocorrência apresenta diálogo com 3 opções.

**Independent Test**: Quickstart Cenários 4 e 5 — cadastrar saída mensal recorrente, navegar para meses futuros e verificar projeções; editar com scope THIS_ONLY e confirmar que outras ocorrências permanecem inalteradas.

### Backend — Engine de Recorrências

- [x] T058 Criar migration `V7__create_recurrence_rules.sql`: tabela `recurrence_rules` com `id BIGINT AUTO_INCREMENT PK`, `frequency ENUM('DAILY','MONTHLY','YEARLY') NOT NULL`, `start_date DATE NOT NULL`, `end_date DATE NULL` — `backend/src/main/resources/db/migration/V7__create_recurrence_rules.sql`
- [x] T059 Criar migration `V8__create_recurrence_exceptions.sql`: tabela `recurrence_exceptions` com `id BIGINT AUTO_INCREMENT PK`, `recurrence_rule_id BIGINT NOT NULL FK→recurrence_rules`, `original_date DATE NOT NULL`, `exception_type ENUM('CANCELLED','MODIFIED') NOT NULL`, `modified_amount DECIMAL(15,2) NULL`, `modified_description TEXT NULL`; índice `idx_recurrence_exceptions_rule(recurrence_rule_id, original_date)` — `backend/src/main/resources/db/migration/V8__create_recurrence_exceptions.sql`
- [x] T060 [P] Criar entidade JPA `RecurrenceRule` — `backend/src/main/java/com/financia/recurrence/RecurrenceRule.java`
- [x] T061 [P] Criar entidade JPA `RecurrenceException` — `backend/src/main/java/com/financia/recurrence/RecurrenceException.java`
- [x] T062 Implementar `RecurrenceProjectionService`: método `generateOccurrences(rule, exceptions, fromDate, toDate): List<LocalDate>` — iterar pela frequência (DAILY: +1 dia, MONTHLY: +1 mês, YEARLY: +1 ano) entre rule.start_date e min(rule.end_date, toDate); excluir datas com exceção `CANCELLED`; para datas com exceção `MODIFIED` retornar valores da exceção — `backend/src/main/java/com/financia/recurrence/RecurrenceProjectionService.java`
- [x] T063 Implementar `RecurrenceEditService`: método `handleEdit(entryId, scope, newValues)` — scope `THIS_ONLY`: inserir `RecurrenceException(MODIFIED, original_date, modified_amount, modified_description)`; scope `THIS_AND_FUTURE`: setar `rule.end_date = original_date - 1 day` e criar nova `RecurrenceRule` com `start_date = original_date` e novo lançamento base; scope `ALL`: atualizar lançamento base diretamente — `backend/src/main/java/com/financia/recurrence/RecurrenceEditService.java`
- [x] T064 Atualizar `IncomeService.createIncome` para aceitar payload de recorrência opcional: se presente, criar `RecurrenceRule` e vincular ao `IncomeEntry` com `is_base_recurrence=TRUE` — `backend/src/main/java/com/financia/income/IncomeService.java`
- [x] T065 Atualizar `ExpenseService.createExpense` para aceitar payload de recorrência opcional com a mesma lógica — `backend/src/main/java/com/financia/expense/ExpenseService.java`
- [x] T066 Atualizar `HistoryService` para: após buscar lançamentos persistidos, chamar `RecurrenceProjectionService` para gerar ocorrências virtuais futuras para cada regra ativa no período; mesclar ao array de rows; marcar ocorrências virtuais com `is_recurring_projection=true`; respeitar `recurrence_exceptions` antes de projetar — `backend/src/main/java/com/financia/history/HistoryService.java`
- [x] T067 Atualizar `PUT /api/income/{id}` e `DELETE /api/income/{id}` para aceitar `recurrence_scope` (`THIS_ONLY|THIS_AND_FUTURE|ALL`) no body quando o lançamento for recorrente; despachar para `RecurrenceEditService` — `backend/src/main/java/com/financia/income/IncomeController.java`
- [x] T068 Atualizar `PUT /api/expense/{id}` e `DELETE /api/expense/{id}` com a mesma lógica de `recurrence_scope` — `backend/src/main/java/com/financia/expense/ExpenseController.java`

### Frontend — UI de Recorrências

- [x] T069 [P] [US4] Adicionar ao `IncomeFormComponent` um toggle "Repetir?" e, quando ativado, exibir selector de frequência (Diária / Mensal / Anual) — `frontend/src/app/income/income-form/income-form.component.ts + .html`
- [x] T070 [P] [US4] Adicionar ao `ExpenseFormComponent` o mesmo toggle e selector de frequência — `frontend/src/app/expense/expense-form/expense-form.component.ts + .html`
- [x] T071 [US4] Criar `RecurrenceScopeDialogComponent` usando Angular Material Dialog: ao editar ou excluir um lançamento recorrente (identificado por `recurrence_rule_id != null`), exibir modal com três opções: "Somente esta ocorrência" / "Esta e todas as futuras" / "Todas as ocorrências"; retornar a opção selecionada (THIS_ONLY / THIS_AND_FUTURE / ALL) para o componente chamador — `frontend/src/app/shared/components/recurrence-scope-dialog/recurrence-scope-dialog.component.ts + .html`
- [x] T072 [US4] Atualizar `HistoryComponent` para exibir ocorrências projetadas (`is_recurring_projection=true`) em itálico com ícone de repetição; ao clicar em editar/excluir uma ocorrência projetada, abrir `RecurrenceScopeDialogComponent` antes de enviar a requisição — `frontend/src/app/history/history.component.ts`

**Checkpoint (US4)**: Lançamento recorrente mensal aparece projetado nos próximos meses; editar uma ocorrência via "Somente esta" não afeta as demais.

---

## Phase 7: User Story 5 — Painel de Relatórios com Exportação (P3)

**Goal**: Usuário acessa painel de relatórios com filtros por período, visualiza totais, agrupamentos por categoria e projeções em gráfico; exporta CSV.

**Independent Test**: Quickstart Cenário 6 — GET /reports?filter=MONTH&year=2026&month=10 retorna totals corretos batendo com o histórico; GET /reports/export retorna CSV com Content-Type text/csv.

### Backend — Engine de Relatórios

- [x] T073 Implementar `ReportService`: (a) parsear filtro (DATE_RANGE / MONTH / YEAR) e converter para from/to LocalDate; (b) calcular `total_income` = SUM(income_entries.amount WHERE user_id AND date BETWEEN); (c) calcular `total_expenses_confirmed` = SUM(expense_entries.amount WHERE CONFIRMED AND date BETWEEN); (d) calcular `total_credit_card` = SUM(expense_entries.amount WHERE PENDING_CREDIT AND date BETWEEN); (e) calcular `savings` = total_income - total_expenses_confirmed - total_credit_card; (f) calcular `category_breakdown` agrupando por `category_name_snapshot` com percentage = (amount/total_expenses)*100, incluir "Sem categoria" para expenses sem categoria; (g) calcular `projections.income`, `projections.expenses`, `projections.savings` como arrays `{date, cumulative}` via GROUP BY date ordenado por data — `backend/src/main/java/com/financia/report/ReportService.java`
- [x] T074 Implementar `GET /api/reports` em `ReportController`: aceitar query params `from/to` OU `filter=MONTH&year&month` OU `filter=YEAR&year`; validar que ao menos um tipo de filtro foi fornecido (422); chamar ReportService; retornar `ReportResponseDTO` — `backend/src/main/java/com/financia/report/ReportController.java`
- [x] T075 Implementar `GET /api/reports/export` no mesmo `ReportController`: mesmos query params; gerar CSV com cabeçalho `Data,Tipo,Descricao,Categoria,Fonte,Valor`; retornar com `Content-Type: text/csv` e `Content-Disposition: attachment; filename="relatorio-{period}.csv"` — `backend/src/main/java/com/financia/report/ReportController.java`

### Frontend — Painel de Relatórios

- [x] T076 [P] [US5] Criar `ReportFilterComponent` com 3 modos de filtro: (1) Date Range — date pickers de início e fim; (2) Mês fechado — year picker + month picker; (3) Ano fechado — year picker; botão "Gerar Relatório" dispara a busca — `frontend/src/app/reports/report-filter/report-filter.component.ts + .html`
- [x] T077 [P] [US5] Criar `ReportTotalsComponent` exibindo cards: Total de Entradas, Total de Saídas, Total Cartão de Crédito, Economia do Período — `frontend/src/app/reports/report-totals/report-totals.component.ts + .html`
- [x] T078 [P] [US5] Criar `ReportCategoriesComponent` exibindo tabela com: Categoria, Valor, % do Total de Saídas; ordenada do maior para o menor valor — `frontend/src/app/reports/report-categories/report-categories.component.ts + .html`
- [x] T079 [US5] Criar `ReportChartsComponent` usando Chart.js (via ng2-charts) com 3 gráficos de linha: Projeção de Entradas ao longo do período, Projeção de Saídas, Projeção de Economia; usar dados de `projections.income`, `projections.expenses`, `projections.savings` do response — `frontend/src/app/reports/report-charts/report-charts.component.ts + .html`
- [x] T080 [US5] Implementar Angular `ReportsService`: `getReport(filter): Observable<ReportResponse>`; `exportCsv(filter)` — disparar download do arquivo CSV via criação dinâmica de `<a>` com `blob:` URL — `frontend/src/app/reports/reports.service.ts`
- [x] T081 [US5] Compor `ReportsPageComponent` integrando: ReportFilterComponent (ao topo), ReportTotalsComponent, ReportChartsComponent, ReportCategoriesComponent; botão "Exportar CSV" visível quando relatório está carregado; chamar ReportsService.exportCsv com o mesmo filtro ativo — `frontend/src/app/reports/reports.component.ts + .html`

**Checkpoint (US5)**: Painel exibe totais, gráficos e agrupamento por categoria para o período selecionado; exportação CSV funciona com os dados corretos.

---

## Phase 8: Polish & Funcionalidades Transversais

**Purpose**: Perfil do usuário, configurações de tema, página de Configurações, validações finais.

- [x] T082 Implementar `GET /api/users/me`, `PUT /api/users/me` (name, email, theme_preference), `PUT /api/users/me/password` (validar `current_password` com BCrypt antes de alterar, retornar 401 se incorreta), `DELETE /api/users/me` (deletar usuário e todos os dados — cascade) em `UserController` — `backend/src/main/java/com/financia/user/UserController.java`
- [x] T083 [P] Criar `ProfileComponent` Angular com formulário de edição de nome/email e formulário separado de troca de senha; exibir mensagem de sucesso/erro após cada ação — `frontend/src/app/profile/profile.component.ts + .html`
- [x] T084 [P] Criar `ThemeService` Angular: ao iniciar o app, chamar GET /users/me e aplicar classe CSS `dark-theme` no elemento `<body>` se `theme_preference='DARK'`; método `toggleTheme()` que chama PUT /users/me e atualiza a classe — `frontend/src/app/shared/services/theme.service.ts`
- [x] T085 [P] Criar `SettingsComponent` Angular como página agregadora com links para sub-seções: Fontes de Receita, Categorias de Gasto, Fontes de Pagamento, e toggle de Tema (Dark/Light) integrado com ThemeService — `frontend/src/app/settings/settings.component.ts + .html`
- [ ] T086 Executar todos os 8 cenários do [quickstart.md](./quickstart.md) end-to-end e registrar resultados: Cenário 1 (saldo básico), 2 (retroativo), 3 (cartão pendente/fatura), 4 (recorrência mensal), 5 (editar ocorrência), 6 (relatório + CSV), 7 (dark mode), 8 (exclusão órfã)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: Sem dependências — iniciar imediatamente
- **Phase 2 (Foundational)**: Depende de Phase 1 — **BLOQUEIA todas as User Stories**
- **Phase 3 (US1)**: Depende de Phase 2 — entidades income/expense criadas aqui são pré-requisito para US2/US3
- **Phase 4 (US2)**: Depende de Phase 2 — pode rodar em paralelo com US3 após Foundational
- **Phase 5 (US3)**: Depende de Phase 2 — pode rodar em paralelo com US2 após Foundational
- **Phase 6 (US4)**: Depende de Phase 3, 4 e 5 — recorrências são adicionadas a income/expense existentes
- **Phase 7 (US5)**: Depende de Phase 4 e 5 — relatórios precisam de dados de income/expense
- **Phase 8 (Polish)**: Depende de todas as fases de User Stories

### User Story Dependencies

- **US1 (History/Balance)**: Pode iniciar após Foundational — retorna dados vazios até US2/US3 terem entradas
- **US2 (Income)**: Pode iniciar após Foundational — independente de US3
- **US3 (Expense + CC)**: Pode iniciar após Foundational — independente de US2 (pode rodar em paralelo)
- **US4 (Recurrence)**: Depende de US2 e US3 — recorrências se apoiam nos modelos de income/expense
- **US5 (Reports)**: Depende de US2 e US3 — relatórios agregam dados de income/expense

### Within Each User Story

- Backend: migrations → entities → repositories → services → controllers
- Frontend: services → components → integração com backend
- Implementar antes de avançar para a próxima story

---

## Parallel Opportunities

### Phase 2 — Paralelo após iniciar Foundational

```
Parallel group A (backend):
  T008 UserRepository
  T010 JwtAuthenticationFilter
  T014 GlobalExceptionHandler

Parallel group B (frontend):
  T015 LoginComponent
  T017 JwtInterceptor
  T018 AuthGuard
```

### Phase 3 — US1 (Saldo e Histórico)

```
Parallel (backend):
  T023 IncomeEntry entity
  T024 ExpenseEntry entity
  T025 Repositories

Parallel (frontend):
  T029 HistoryService Angular
```

### Phase 4/5 — US2 e US3 em paralelo (após Phase 2)

```
Developer A: T032–T042 (Income Sources + Income entries)
Developer B: T043–T057 (Categories + Payment Methods + Expenses)
```

### Phase 7 — US5 (Relatórios)

```
Parallel (frontend):
  T076 ReportFilterComponent
  T077 ReportTotalsComponent
  T078 ReportCategoriesComponent
```

---

## Implementation Strategy

### MVP First (US1 + US2 + US3 — todas P1)

1. Completar Phase 1: Setup
2. Completar Phase 2: Foundational (CRÍTICO)
3. Completar Phase 3: US1 — motor de histórico e saldo
4. Completar Phase 4: US2 — entradas
5. Completar Phase 5: US3 — saídas e cartão
6. **PARAR E VALIDAR**: Cenários 1, 2 e 3 do quickstart.md
7. Demonstrar MVP funcional

### Incremental Delivery

1. Setup + Foundational → autenticação funcional
2. US1 + US2 + US3 (P1) → **MVP demonstrável**: registrar entradas/saídas, ver histórico, saldo correto, cartão pendente
3. US4 (P2) → lançamentos recorrentes com projeção futura
4. US5 (P3) → painel de relatórios com exportação CSV
5. Polish → perfil, dark mode, validações finais

### Parallel Team Strategy

Com 2 desenvolvedores após Phase 2 completa:
- **Dev A**: US1 + US4 (motor de histórico + engine de recorrências)
- **Dev B**: US2 + US3 (income/expense + categorias/cartão)
- Juntos: US5 + Polish
