package com.financia.expense;

import com.financia.common.BusinessException;
import com.financia.common.RecurrenceScope;
import com.financia.expense.dto.ExpenseRequest;
import com.financia.expense.dto.ExpenseResponse;
import com.financia.paymentmethod.PaymentMethod;
import com.financia.paymentmethod.PaymentMethodRepository;
import com.financia.paymentmethod.PaymentMethodType;
import com.financia.recurrence.RecurrenceEditService;
import com.financia.recurrence.RecurrenceRule;
import com.financia.recurrence.RecurrenceRuleRepository;
import com.financia.user.User;
import com.financia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseEntryRepository expenseEntryRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final UserRepository userRepository;
    private final RecurrenceEditService recurrenceEditService;
    private final RecurrenceRuleRepository recurrenceRuleRepository;

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long userId, Long id) {
        return ExpenseResponse.from(getOwned(userId, id));
    }

    @Transactional
    public ExpenseResponse create(Long userId, ExpenseRequest request) {
        validate(request);
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        ExpenseEntry entry = new ExpenseEntry();
        entry.setUser(user);
        applyFields(userId, entry, request);
        if (request.getRecurrence() != null && request.getRecurrence().getFrequency() != null) {
            RecurrenceRule rule = recurrenceEditService.createRule(
                    request.getRecurrence().getFrequency(),
                    request.getRecurrence().getStartDate() != null ? request.getRecurrence().getStartDate() : request.getDate(),
                    request.getRecurrence().getEndDate()
            );
            entry.setRecurrenceRule(rule);
            entry.setBaseRecurrence(true);
        }
        return ExpenseResponse.from(expenseEntryRepository.save(entry));
    }

    @Transactional
    public ExpenseResponse update(Long userId, Long id, ExpenseRequest request) {
        validate(request);
        ExpenseEntry entry = getOwned(userId, id);
        if (entry.getRecurrenceRule() != null) {
            RecurrenceScope scope = request.getRecurrenceScope();
            LocalDate occurrenceDate = request.getDate() != null ? request.getDate() : entry.getDate();
            recurrenceEditService.handleEdit(
                    entry.getRecurrenceRule(),
                    occurrenceDate,
                    scope,
                    request.getAmount(),
                    request.getDescription(),
                    rule -> applyFields(userId, entry, request),
                    previous -> {
                        RecurrenceRule newRule = recurrenceEditService.createRule(
                                previous.getFrequency(),
                                request.getDate(),
                                previous.getEndDate()
                        );
                        ExpenseEntry next = new ExpenseEntry();
                        next.setUser(entry.getUser());
                        applyFields(userId, next, request);
                        next.setRecurrenceRule(newRule);
                        next.setBaseRecurrence(true);
                        expenseEntryRepository.save(next);
                    }
            );
            if (scope == RecurrenceScope.THIS_ONLY) {
                return ExpenseResponse.from(entry);
            }
        } else {
            applyFields(userId, entry, request);
        }
        return ExpenseResponse.from(expenseEntryRepository.save(entry));
    }

    @Transactional
    public void delete(Long userId, Long id, RecurrenceScope scope) {
        delete(userId, id, scope, null);
    }

    @Transactional
    public void delete(Long userId, Long id, RecurrenceScope scope, LocalDate occurrenceDate) {
        ExpenseEntry entry = getOwned(userId, id);
        LocalDate occ = occurrenceDate != null ? occurrenceDate : entry.getDate();
        if (entry.getRecurrenceRule() != null) {
            RecurrenceRule rule = entry.getRecurrenceRule();
            recurrenceEditService.handleDelete(rule, occ, scope, () -> {
                expenseEntryRepository.findByUserIdAndRecurrenceRuleIdAndBaseRecurrenceTrue(userId, rule.getId())
                        .ifPresent(expenseEntryRepository::delete);
                recurrenceRuleRepository.delete(rule);
            });
            return;
        }
        if (occurrenceDate != null && !occurrenceDate.equals(entry.getDate())) {
            throw new BusinessException("Lançamento não ocorre nesta data", "date");
        }
        expenseEntryRepository.delete(entry);
    }

    private void applyFields(Long userId, ExpenseEntry entry, ExpenseRequest request) {
        entry.setDate(request.getDate());
        entry.setAmount(request.getAmount());
        entry.setDescription(request.getDescription());
        if (request.getExpenseCategoryId() != null) {
            ExpenseCategory category = categoryRepository.findByIdAndUserId(request.getExpenseCategoryId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
            entry.setExpenseCategory(category);
            entry.setCategoryNameSnapshot(category.getName());
        } else if (entry.getCategoryNameSnapshot() == null) {
            entry.setExpenseCategory(null);
        }
        if (request.getPaymentMethodId() != null) {
            PaymentMethod method = paymentMethodRepository.findByIdAndUserId(request.getPaymentMethodId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Fonte de pagamento não encontrada"));
            entry.setPaymentMethod(method);
            entry.setPaymentMethodNameSnapshot(method.getName());
            entry.setPaymentStatus(method.getType() == PaymentMethodType.CREDIT_CARD
                    ? PaymentStatus.PENDING_CREDIT
                    : PaymentStatus.CONFIRMED);
        } else {
            entry.setPaymentMethod(null);
            if (entry.getPaymentMethodNameSnapshot() == null) {
                entry.setPaymentStatus(PaymentStatus.CONFIRMED);
            }
        }
    }

    private void validate(ExpenseRequest request) {
        if (request.getDate() == null) {
            throw new BusinessException("Data é obrigatória", "date");
        }
        if (request.getAmount() == null) {
            throw new BusinessException("Valor é obrigatório", "amount");
        }
    }

    private ExpenseEntry getOwned(Long userId, Long id) {
        return expenseEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Saída não encontrada"));
    }
}
