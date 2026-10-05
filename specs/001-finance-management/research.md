# Research: Sistema de Gestão Financeira

**Feature**: 001-finance-management | **Date**: 2026-10-04 | **Phase**: 0

---

## Decision 1: Estratégia de Cálculo de Saldo

**Decision**: Cálculo dinâmico via SQL acumulado (`SUM`) a cada requisição de histórico.

**Rationale**: Volume esperado de ~1.800 registros para 5 anos de uso intenso é trivialmente pequeno para um banco relacional. Cálculo on-demand garante consistência automática ao inserir lançamentos retroativos (FR-011) sem nenhuma lógica de invalidação de cache. A complexidade de manter totais pré-calculados (event sourcing, cache por data) não se justifica para um app personal-finance de usuário único.

**Alternatives considered**:
- Stored running total (campo `balance_after` em cada transação): rejeitado — qualquer lançamento retroativo exige UPDATE em cascata em todos os registros posteriores.
- Redis cache de saldo por data: rejeitado — overhead de infraestrutura sem ganho mensurável para o volume de dados esperado.

**Implementation note**: O endpoint de histórico executa uma query que, para cada data no período, calcula `SUM(amount) FILTER (WHERE type = INCOME AND date <= current_date) - SUM(amount) FILTER (WHERE type = EXPENSE AND status = CONFIRMED AND date <= current_date)` sobre todos os lançamentos do usuário.

---

## Decision 2: Modelo de Transações Recorrentes

**Decision**: Padrão "virtual occurrence" — regra armazenada, ocorrências geradas em memória.

**Rationale**: Evita pré-gerar milhares de registros no banco para recorrências longas (ex: aluguel diário por 5 anos = 1.825 registros extras). O backend gera ocorrências futuras em memória quando o histórico de datas futuras é solicitado, com base na regra de recorrência.

**Alternatives considered**:
- Pre-generate all occurrences on save: rejeitado — gera volume excessivo de dados, complexidade na deleção e sem ganho perceptível para o usuário.
- RRULE (iCalendar standard): considerado — complexidade desnecessária para apenas 3 frequências (diária, mensal, anual).

**Exception handling (FR-009a)**:
- "Somente esta ocorrência": cria um `recurrence_exception` com `exception_type = CANCELLED` ou `MODIFIED` para a data original.
- "Esta e todas as futuras": define `end_date` na regra original = data anterior à edição; cria nova `recurrence_rule` a partir da data de edição com os novos valores.
- "Todas as ocorrências": atualiza o lançamento base; a regra permanece, todas as ocorrências futuras refletem o novo valor (exceto as que já têm exceção).

---

## Decision 3: Estado "Pendente" do Cartão de Crédito

**Decision**: Campo `payment_status` enum (`CONFIRMED` | `PENDING_CREDIT`) na entidade Despesa. Consolidação no vencimento é computada em tempo de leitura (sintética), não armazenada.

**Rationale**: Evita duplicidade de dados (registro individual + registro consolidado). O backend, ao montar o histórico para uma data que é dia de vencimento de algum cartão, agrega automaticamente as despesas `PENDING_CREDIT` do período de faturamento correspondente e injeta uma linha sintética "Fatura [Nome do Cartão] — R$ X" nessa data.

**Alternatives considered**:
- Criar registro "fatura consolidada" no banco no dia do vencimento: rejeitado — cria dados derivados persistidos que podem sair de sincronia com os registros originais se o usuário editar uma despesa retroativamente.
- Separar tabela `credit_card_bills`: rejeitado — complexidade desnecessária para o escopo atual.

**Visibility rule**: Despesas `PENDING_CREDIT` aparecem no histórico na data do registro com identificador visual distinto. O saldo corrente (`running_balance`) NÃO inclui despesas `PENDING_CREDIT`. A linha sintética de fatura no dia de vencimento SIM afeta o saldo.

---

## Decision 4: Estrutura de Dados para Projeções de Relatório

**Decision**: Backend retorna arrays de `{date: "YYYY-MM-DD", amount: number}` para cada série (entradas, saídas, economias), calculados via `GROUP BY DATE` com acumulação por data.

**Rationale**: Formato genérico que o frontend pode consumir diretamente para renderizar gráficos de linha (Chart.js, ECharts, ou Angular Material Charts). Economia = entradas acumuladas − saídas acumuladas na mesma data.

**Report response structure**:
```json
{
  "period": { "from": "2026-09-01", "to": "2026-09-30" },
  "totals": {
    "income": 5000.00,
    "expenses": 3200.00,
    "credit_card": 800.00,
    "savings": 1800.00
  },
  "category_breakdown": [
    { "category": "Lazer", "amount": 400.00, "percentage": 12.5 }
  ],
  "projections": {
    "income": [{ "date": "2026-09-05", "cumulative": 5000.00 }],
    "expenses": [{ "date": "2026-09-10", "cumulative": 2400.00 }],
    "savings": [{ "date": "2026-09-10", "cumulative": 2600.00 }]
  }
}
```

**Alternatives considered**:
- Retornar dados brutos e calcular projeções no frontend: rejeitado — concentra lógica de negócio no frontend, dificulta testes.
- Pré-calcular e cachear relatórios: rejeitado — volume pequeno, desnecessário para v1.
