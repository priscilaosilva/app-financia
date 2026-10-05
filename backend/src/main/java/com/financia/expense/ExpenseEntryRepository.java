package com.financia.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseEntryRepository extends JpaRepository<ExpenseEntry, Long> {
    List<ExpenseEntry> findByUserIdAndDateBetweenOrderByDateAsc(Long userId, LocalDate from, LocalDate to);

    List<ExpenseEntry> findByUserIdAndRecurrenceRuleIsNotNull(Long userId);

    Optional<ExpenseEntry> findByIdAndUserId(Long id, Long userId);

    Optional<ExpenseEntry> findByUserIdAndRecurrenceRuleIdAndBaseRecurrenceTrue(Long userId, Long ruleId);

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntry e
            WHERE e.user.id = :userId AND e.paymentStatus = :status AND e.date <= :date
            """)
    BigDecimal sumAmountByUserIdAndStatusAndDateLessThanEqual(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("date") LocalDate date
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntry e
            WHERE e.user.id = :userId AND e.paymentStatus = :status AND e.date BETWEEN :from AND :to
            """)
    BigDecimal sumAmountByUserIdAndStatusAndDateBetween(
            @Param("userId") Long userId,
            @Param("status") PaymentStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    List<ExpenseEntry> findByUserIdAndPaymentMethodIdAndPaymentStatusAndDateBetween(
            Long userId, Long paymentMethodId, PaymentStatus paymentStatus, LocalDate from, LocalDate to
    );
}
