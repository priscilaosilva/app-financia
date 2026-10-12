# Constituição do Financia

## Core Principles

### I. Produto em português, dados do dono

A interface visível é em português. Cada usuário só vê e altera os próprios lançamentos, fontes, categorias e pagamentos. Segredos (senhas de banco, JWT, `.env`) não entram em commits nem na documentação pública.

### II. Histórico como fonte da verdade temporal

O histórico diário (todos os dias do calendário do período) é a visão principal de saldo. Relatórios de evolução no tempo reutilizam o mesmo recorte (`GET /history`) em vez de inventar outra série acumulada de entradas/saídas.

### III. Overlays, não navegar para registrar

Lançar e editar no histórico acontece em modal sobre a planilha. Fechar/Escape cancelam; o fundo não fecha o overlay de lançamento (contrato da feature 004).

### IV. Recorrência com alcance explícito

Apagar ou editar um lançamento recorrente pergunta o alcance (somente esta, esta e futuras, todas) **antes** do HTTP. Cancelar o diálogo não grava.

### V. Release notes no polish (NON-NEGOTIABLE)

Toda implementação Spec Kit (`/speckit-implement`) **atualiza** `RELEASE-NOTES.md` na raiz no polish, em português, com as mudanças visíveis da feature. Features futuras não se encerram sem essa seção.

## Documentação

- `README.md`: o que o produto faz (telas e regras).
- `app-settings.md`: como rodar (sem senha real).
- `RELEASE-NOTES.md`: histórico de entregas 001+.
- Specs em `specs/` descrevem o comportamento; a constituição manda no conflito.

## Stack

Angular 17 (SPA) + Spring Boot 3.3 + MySQL 8. JWT na sessão. Sem expor exploits nem credenciais.

## Governance

Esta constituição prevalece sobre atalhos de implementação. Emenda: alterar este arquivo, bump de versão e nota em `RELEASE-NOTES.md` se o usuário perceber a mudança.

**Version**: 1.1.0 | **Ratified**: 2026-10-11 | **Last Amended**: 2026-10-11
