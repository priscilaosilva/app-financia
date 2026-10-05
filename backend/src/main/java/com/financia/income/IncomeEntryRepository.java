package com.financia.income;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeEntryRepository extends JpaRepository<IncomeEntry, Long> {
    List<IncomeEntry> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    List<IncomeEntry> findByUserIdAndRecurrenceRuleIsNotNull(Long userId);

    Optional<IncomeEntry> findByIdAndUserId(Long id, Long userId);

    Optional<IncomeEntry> findByUserIdAndRecurrenceRuleIdAndBaseRecurrenceTrue(Long userId, Long ruleId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM IncomeEntry e WHERE e.user.id = :userId AND e.date <= :date")
    BigDecimal sumAmountByUserIdAndDateLessThanEqual(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM IncomeEntry e WHERE e.user.id = :userId AND e.date BETWEEN :from AND :to")
    BigDecimal sumAmountByUserIdAndDateBetween(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
