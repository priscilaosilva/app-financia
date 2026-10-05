package com.financia.recurrence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurrenceProjectionService {

    private final RecurrenceExceptionRepository exceptionRepository;

    public List<ProjectedOccurrence> generateOccurrences(
            RecurrenceRule rule,
            BigDecimal baseAmount,
            String baseDescription,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Map<LocalDate, RecurrenceException> exceptions = exceptionRepository.findByRuleId(rule.getId()).stream()
                .collect(Collectors.toMap(RecurrenceException::getOriginalDate, Function.identity(), (a, b) -> b));

        LocalDate cursor = rule.getStartDate();
        LocalDate limit = rule.getEndDate() == null || rule.getEndDate().isAfter(toDate) ? toDate : rule.getEndDate();
        List<ProjectedOccurrence> occurrences = new ArrayList<>();

        while (!cursor.isAfter(limit)) {
            if (!cursor.isBefore(fromDate)) {
                RecurrenceException exception = exceptions.get(cursor);
                if (exception == null) {
                    occurrences.add(new ProjectedOccurrence(cursor, baseAmount, baseDescription, false, false));
                } else if (exception.getExceptionType() == ExceptionType.CANCELLED) {
                    occurrences.add(new ProjectedOccurrence(cursor, BigDecimal.ZERO, baseDescription, true, false));
                } else {
                    BigDecimal amount = exception.getModifiedAmount() != null ? exception.getModifiedAmount() : baseAmount;
                    String description = exception.getModifiedDescription() != null
                            ? exception.getModifiedDescription()
                            : baseDescription;
                    occurrences.add(new ProjectedOccurrence(cursor, amount, description, false, true));
                }
            }
            cursor = next(cursor, rule.getFrequency());
        }
        return occurrences;
    }

    public LocalDate next(LocalDate date, Frequency frequency) {
        return switch (frequency) {
            case DAILY -> date.plusDays(1);
            case MONTHLY -> date.plusMonths(1);
            case YEARLY -> date.plusYears(1);
        };
    }
}
