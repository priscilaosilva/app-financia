# Data Model: Edição no histórico, gráfico de evolução, login e documentação

**Feature**: 005-edit-mobile-docs | **Date**: 2026-10-11

Sem tabelas novas. Reusa `income_entries`, `expense_entries`, `recurrence_rules` e o histórico por dia.

## E-mail de acesso (UI)

| Campo | Regras |
|-------|--------|
| valor | trim; `Validators.email` + required |
| envio | bloqueado se inválido (login, cadastro, forgot) |

Não persiste nada além do `users.email` já existente.

## Lançamento em edição (não é tabela nova)

Estado no histórico:

| Campo | Origem |
|-------|--------|
| id, type (`INCOME` \| `EXPENSE`) | item selecionado em Gerenciar |
| date, amount, description, source/category/payment ids | `GET /income/{id}` ou `GET /expense/{id}` |
| is_recurring | histórico |
| kind no modal | começa no `type`; a pessoa pode trocar |
| recurrence_scope | diálogo após Salvar, se recorrente |

**Transições**:

```text
Gerenciar (0 ou N>1 marcados) → Editar oculto
Gerenciar (1 marcado) → Editar visível
  → modal edição (GET prefill)
     → Escape/Fechar → Gerenciar intacto, nada gravado
     → Salvar, não recorrente
          mesmo tipo → PUT
          outro tipo → DELETE + POST
          fecha edição; recarrega dia; Gerenciar aberto (ou fecha se vazio)
     → Salvar, recorrente → diálogo de alcance
          cancelar alcance → continua no modal de edição, sem HTTP de gravação
          confirmar → PUT ou DELETE+POST com scope; depois igual ao não recorrente
```

Troca de tipo: o registro antigo deixa de existir no alcance escolhido; o novo tipo é criado com data, valor, descrição (e repetir se marcado). Fonte/categoria/pagamento do tipo destino só se a pessoa preencheu depois da troca.

## Série do gráfico

Não persistida. Por cada `HistoryRow` do `from`–`to` do relatório:

| Série | Campo |
|-------|--------|
| Entradas do dia | `daily_income` |
| Saídas do dia | `daily_expense_confirmed + daily_expense_pending` |
| Saldo corrido | `running_balance` |

Dias sem movimento: 0 / 0 / saldo do dia anterior (já calculado no histórico).

## Documentos (repositório)

| Arquivo | Papel |
|---------|--------|
| README.md | documentação funcional |
| app-settings.md | como rodar |
| RELEASE-NOTES.md | entregas 001–005 e posteriores |

## Validação (já vigente + edição)

- Data e valor > 0: iguais ao create
- `recurrence_scope` obrigatório se o lançamento tem regra (422 como na exclusão)
- GET/PUT/DELETE 404 se id de outro usuário
