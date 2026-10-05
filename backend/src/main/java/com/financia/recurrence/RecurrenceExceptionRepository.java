package com.financia.recurrence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurrenceExceptionRepository extends JpaRepository<RecurrenceException, Long> {
    List<RecurrenceException> findByRuleId(Long ruleId);

    Optional<RecurrenceException> findByRuleIdAndOriginalDate(Long ruleId, LocalDate originalDate);
}
