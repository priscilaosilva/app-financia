package com.financia.recurrence;

import com.financia.common.BusinessException;
import com.financia.common.RecurrenceScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class RecurrenceEditService {

    private final RecurrenceRuleRepository ruleRepository;
    private final RecurrenceExceptionRepository exceptionRepository;

    @Transactional
    public RecurrenceRule handleEdit(
            RecurrenceRule currentRule,
            LocalDate originalDate,
            RecurrenceScope scope,
            BigDecimal newAmount,
            String newDescription,
            Consumer<RecurrenceRule> updateBase,
            Consumer<RecurrenceRule> createNewSeries
    ) {
        if (scope == null) {
            throw new BusinessException("recurrence_scope é obrigatório para lançamentos recorrentes", "recurrence_scope");
        }
        return switch (scope) {
            case THIS_ONLY -> {
                upsertException(currentRule, originalDate, ExceptionType.MODIFIED, newAmount, newDescription);
                yield currentRule;
            }
            case THIS_AND_FUTURE -> {
                currentRule.setEndDate(originalDate.minusDays(1));
                ruleRepository.save(currentRule);
                createNewSeries.accept(currentRule);
                yield currentRule;
            }
            case ALL -> {
                updateBase.accept(currentRule);
                yield currentRule;
            }
        };
    }

    @Transactional
    public void handleDelete(RecurrenceRule currentRule, LocalDate originalDate, RecurrenceScope scope, Runnable deleteAll) {
        if (scope == null) {
            throw new BusinessException("recurrence_scope é obrigatório para lançamentos recorrentes", "recurrence_scope");
        }
        switch (scope) {
            case THIS_ONLY -> upsertException(currentRule, originalDate, ExceptionType.CANCELLED, null, null);
            case THIS_AND_FUTURE -> {
                currentRule.setEndDate(originalDate.minusDays(1));
                ruleRepository.save(currentRule);
                upsertException(currentRule, originalDate, ExceptionType.CANCELLED, null, null);
            }
            case ALL -> deleteAll.run();
        }
    }

    public RecurrenceRule createRule(Frequency frequency, LocalDate startDate, LocalDate endDate) {
        RecurrenceRule rule = new RecurrenceRule();
        rule.setFrequency(frequency);
        rule.setStartDate(startDate);
        rule.setEndDate(endDate);
        return ruleRepository.save(rule);
    }

    private void upsertException(
            RecurrenceRule rule,
            LocalDate originalDate,
            ExceptionType type,
            BigDecimal amount,
            String description
    ) {
        RecurrenceException exception = exceptionRepository
                .findByRuleIdAndOriginalDate(rule.getId(), originalDate)
                .orElseGet(RecurrenceException::new);
        exception.setRule(rule);
        exception.setOriginalDate(originalDate);
        exception.setExceptionType(type);
        exception.setModifiedAmount(amount);
        exception.setModifiedDescription(description);
        exceptionRepository.save(exception);
    }
}
