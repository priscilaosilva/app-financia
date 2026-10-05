# Data Model: Sistema de Gestão Financeira

**Feature**: 001-finance-management | **Date**: 2026-10-04 | **Phase**: 1

---

## Entidades e Tabelas

### `users`

Representa o usuário autenticado. Cada usuário é proprietário isolado de todos os dados financeiros.

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `name` | VARCHAR(100) | NOT NULL | Nome de exibição |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email de autenticação |
| `password_hash` | VARCHAR(255) | NOT NULL | Hash bcrypt da senha |
| `theme_preference` | ENUM('LIGHT','DARK') | NOT NULL, DEFAULT 'LIGHT' | Preferência visual |
| `created_at` | DATETIME | NOT NULL, DEFAULT NOW() | Data de criação |
| `updated_at` | DATETIME | NOT NULL | Última atualização |

**Validações**: email deve ser único no sistema; password_hash nunca retornado na API.

---

### `income_sources`

Fontes de receita definidas pelo usuário (ex: "Empresa X", "Vendas de bolo").

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `user_id` | BIGINT | FK → users.id, NOT NULL | Proprietário |
| `name` | VARCHAR(100) | NOT NULL | Nome da fonte |
| `active` | BOOLEAN | NOT NULL, DEFAULT TRUE | FALSE = excluída (órfã) |
| `created_at` | DATETIME | NOT NULL | Data de criação |

**Regra de integridade (FR-022)**: ao "excluir" uma fonte, `active = FALSE`. Registros de entrada existentes mantêm `source_name_snapshot` (ver abaixo). A fonte não aparece mais em listas de seleção.

---

### `expense_categories`

Categorias de gastos definidas pelo usuário (ex: "Lazer", "Aluguel").

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `user_id` | BIGINT | FK → users.id, NOT NULL | Proprietário |
| `name` | VARCHAR(100) | NOT NULL | Nome da categoria |
| `active` | BOOLEAN | NOT NULL, DEFAULT TRUE | FALSE = excluída (órfã) |
| `created_at` | DATETIME | NOT NULL | Data de criação |

---

### `payment_methods`

Fontes de pagamento: conta corrente, reserva ou cartão de crédito.

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `user_id` | BIGINT | FK → users.id, NOT NULL | Proprietário |
| `name` | VARCHAR(100) | NOT NULL | Nome (ex: "Nubank", "Reserva") |
| `type` | ENUM('CHECKING','SAVINGS','CREDIT_CARD') | NOT NULL | Tipo da conta |
| `due_day` | TINYINT | NULL | Dia do mês de vencimento (apenas CREDIT_CARD) |
| `billing_start_day` | TINYINT | NULL | Dia de início do período de faturamento |
| `billing_end_day` | TINYINT | NULL | Dia de fim do período de faturamento |
| `active` | BOOLEAN | NOT NULL, DEFAULT TRUE | FALSE = excluída (órfã) |
| `created_at` | DATETIME | NOT NULL | Data de criação |

**Validação (FR-006)**: se `type = CREDIT_CARD`, então `due_day`, `billing_start_day` e `billing_end_day` são obrigatórios (NOT NULL efetivo via validação de aplicação).

---

### `recurrence_rules`

Regras de recorrência para entradas e saídas.

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `frequency` | ENUM('DAILY','MONTHLY','YEARLY') | NOT NULL | Frequência |
| `start_date` | DATE | NOT NULL | Data da primeira ocorrência |
| `end_date` | DATE | NULL | Data da última ocorrência (NULL = indefinida) |

---

### `recurrence_exceptions`

Exceções a ocorrências de lançamentos recorrentes (ocorrências editadas ou canceladas individualmente).

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `recurrence_rule_id` | BIGINT | FK → recurrence_rules.id, NOT NULL | Regra afetada |
| `original_date` | DATE | NOT NULL | Data da ocorrência original |
| `exception_type` | ENUM('CANCELLED','MODIFIED') | NOT NULL | Tipo da exceção |
| `modified_amount` | DECIMAL(15,2) | NULL | Novo valor (se MODIFIED) |
| `modified_description` | TEXT | NULL | Nova descrição (se MODIFIED) |

**Uso (FR-009a)**:
- "Somente esta ocorrência — cancelar": insere `CANCELLED` para a data.
- "Somente esta ocorrência — editar": insere `MODIFIED` com novos valores.
- "Esta e todas as futuras": `end_date` da regra atual = data anterior; nova regra criada a partir da data.
- "Todas as ocorrências": atualiza o lançamento base diretamente.

---

### `income_entries`

Registros de entradas financeiras.

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `user_id` | BIGINT | FK → users.id, NOT NULL | Proprietário |
| `date` | DATE | NOT NULL | Data da entrada (obrigatório, FR-002) |
| `amount` | DECIMAL(15,2) | NOT NULL, > 0 | Valor em BRL |
| `income_source_id` | BIGINT | FK → income_sources.id, NULL | Fonte (opcional) |
| `source_name_snapshot` | VARCHAR(100) | NULL | Nome da fonte no momento do registro |
| `description` | TEXT | NULL | Descrição livre (opcional) |
| `recurrence_rule_id` | BIGINT | FK → recurrence_rules.id, NULL | Regra de recorrência (se recorrente) |
| `is_base_recurrence` | BOOLEAN | NOT NULL, DEFAULT FALSE | TRUE = este é o lançamento base da série |
| `created_at` | DATETIME | NOT NULL | Data de cadastro no sistema |

**Snapshot**: `source_name_snapshot` é preenchido com o nome da fonte no momento do cadastro, garantindo que exclusões futuras da fonte (FR-022) não apaguem o nome histórico.

---

### `expense_entries`

Registros de saídas financeiras.

| Campo | Tipo | Restrições | Descrição |
|-------|------|-----------|-----------|
| `id` | BIGINT | PK, AUTO_INCREMENT | Identificador único |
| `user_id` | BIGINT | FK → users.id, NOT NULL | Proprietário |
| `date` | DATE | NOT NULL | Data da despesa (obrigatório, FR-008) |
| `amount` | DECIMAL(15,2) | NOT NULL, > 0 | Valor em BRL |
| `expense_category_id` | BIGINT | FK → expense_categories.id, NULL | Categoria (opcional) |
| `category_name_snapshot` | VARCHAR(100) | NULL | Nome da categoria no momento do registro |
| `payment_method_id` | BIGINT | FK → payment_methods.id, NULL | Fonte de pagamento (opcional) |
| `payment_method_name_snapshot` | VARCHAR(100) | NULL | Nome da fonte de pagamento no momento do registro |
| `payment_status` | ENUM('CONFIRMED','PENDING_CREDIT') | NOT NULL, DEFAULT 'CONFIRMED' | Status de débito |
| `description` | TEXT | NULL | Descrição livre (opcional) |
| `recurrence_rule_id` | BIGINT | FK → recurrence_rules.id, NULL | Regra de recorrência |
| `is_base_recurrence` | BOOLEAN | NOT NULL, DEFAULT FALSE | TRUE = lançamento base da série |
| `created_at` | DATETIME | NOT NULL | Data de cadastro no sistema |

**Regra de payment_status (FR-007)**:
- Se `payment_method.type = CREDIT_CARD`: `payment_status = PENDING_CREDIT` automaticamente.
- Se `payment_method.type = CHECKING | SAVINGS` ou NULL: `payment_status = CONFIRMED`.
- Despesas `PENDING_CREDIT` **não** contribuem para o saldo corrente.
- No dia de vencimento do cartão, o backend injeta uma linha sintética com o total `PENDING_CREDIT` do período de faturamento.

---

## Relacionamentos

```
users ──< income_sources
users ──< expense_categories
users ──< payment_methods
users ──< income_entries >── income_sources (nullable)
users ──< expense_entries >── expense_categories (nullable)
                          >── payment_methods (nullable)
income_entries >── recurrence_rules ──< recurrence_exceptions
expense_entries >── recurrence_rules ──< recurrence_exceptions
```

---

## Transições de Estado

### Despesa — `payment_status`

```
[Registro com cartão de crédito]
        │
        ▼
PENDING_CREDIT ──── (dia de vencimento do cartão) ───► [Linha sintética no histórico]
        │
        └── Visível no histórico como "pendente", não afeta saldo

[Registro com outra fonte ou sem fonte]
        │
        ▼
CONFIRMED ──── Afeta saldo imediatamente na data informada
```

### Recorrência — Exceções

```
Ocorrência virtual gerada (em memória)
        │
        ├── [Usuário cancela esta] ──► exception CANCELLED inserida
        ├── [Usuário edita esta] ──► exception MODIFIED inserida
        ├── [Usuário edita esta e futuras] ──► end_date na regra atual + nova regra
        └── [Usuário edita todas] ──► lançamento base atualizado diretamente
```

---

## Índices Recomendados

```sql
-- Consultas de histórico (mais frequentes)
CREATE INDEX idx_income_user_date ON income_entries(user_id, date);
CREATE INDEX idx_expense_user_date ON expense_entries(user_id, date);
CREATE INDEX idx_expense_status ON expense_entries(user_id, payment_status, date);

-- Consultas de cadastros auxiliares
CREATE INDEX idx_income_sources_user ON income_sources(user_id, active);
CREATE INDEX idx_categories_user ON expense_categories(user_id, active);
CREATE INDEX idx_payment_methods_user ON payment_methods(user_id, active);

-- Recorrências
CREATE INDEX idx_recurrence_exceptions_rule ON recurrence_exceptions(recurrence_rule_id, original_date);
```
