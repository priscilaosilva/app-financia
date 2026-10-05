package com.financia.expense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategory, Long> {
    List<ExpenseCategory> findByUserIdAndActiveTrue(Long userId);

    Optional<ExpenseCategory> findByIdAndUserId(Long id, Long userId);
}
