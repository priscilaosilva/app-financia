package com.financia.history;

import com.financia.common.BusinessException;
import com.financia.common.RecurrenceScope;
import com.financia.expense.ExpenseEntry;
import com.financia.expense.ExpenseEntryRepository;
import com.financia.expense.ExpenseService;
import com.financia.history.dto.DayDeletionItem;
import com.financia.history.dto.DayDeletionRequest;
import com.financia.history.dto.HistoryEntryDto;
import com.financia.history.dto.HistoryRowDto;
import com.financia.income.IncomeEntry;
import com.financia.income.IncomeEntryRepository;
import com.financia.income.IncomeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HistoryDeletionService {

    private final HistoryService historyService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final IncomeEntryRepository incomeEntryRepository;
    private final ExpenseEntryRepository expenseEntryRepository;

    @Transactional
    public void deleteDayEntries(Long userId, DayDeletionRequest request) {
        if (request.getDate() == null) {
            throw new BusinessException("Data é obrigatória", "date");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Selecione ao menos um registro", "items");
        }

        List<HistoryRowDto> rows = historyService.history(userId, request.getDate(), request.getDate()).getRows();
        List<HistoryEntryDto> dayEntries = rows.isEmpty() ? List.of() : rows.get(0).getEntries();

        Set<String> seen = new HashSet<>();
        boolean needsScope = false;
        for (DayDeletionItem item : request.getItems()) {
            String type = item.getType() == null ? "" : item.getType().trim().toUpperCase();
            if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
                throw new BusinessException("Tipo inválido para exclusão", "type");
            }
            String key = type + ":" + item.getId();
            if (!seen.add(key)) {
                continue;
            }
            HistoryEntryDto match = dayEntries.stream()
                    .filter(entry -> item.getId().equals(entry.getId()) && type.equals(entry.getType()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessException("Lançamento não ocorre nesta data", "date"));

            if (isRecurring(userId, type, item.getId())) {
                needsScope = true;
            } else if (match.isRecurringProjection()) {
                needsScope = true;
            }
        }

        if (needsScope && request.getRecurrenceScope() == null) {
            throw new BusinessException("recurrence_scope é obrigatório para lançamentos recorrentes", "recurrence_scope");
        }

        RecurrenceScope scope = request.getRecurrenceScope();
        seen.clear();
        for (DayDeletionItem item : request.getItems()) {
            String type = item.getType().trim().toUpperCase();
            if (!seen.add(type + ":" + item.getId())) {
                continue;
            }
            if ("INCOME".equals(type)) {
                incomeService.delete(userId, item.getId(), scope, request.getDate());
            } else {
                expenseService.delete(userId, item.getId(), scope, request.getDate());
            }
        }
    }

    private boolean isRecurring(Long userId, String type, Long id) {
        if ("INCOME".equals(type)) {
            IncomeEntry entry = incomeEntryRepository.findByIdAndUserId(id, userId)
                    .orElseThrow(() -> new EntityNotFoundException("Entrada não encontrada"));
            return entry.getRecurrenceRule() != null;
        }
        ExpenseEntry entry = expenseEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Saída não encontrada"));
        return entry.getRecurrenceRule() != null;
    }
}
