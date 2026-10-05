package com.financia.income;

import com.financia.common.BusinessException;
import com.financia.common.RecurrenceScope;
import com.financia.income.dto.IncomeRequest;
import com.financia.income.dto.IncomeResponse;
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
public class IncomeService {

    private final IncomeEntryRepository incomeEntryRepository;
    private final IncomeSourceRepository incomeSourceRepository;
    private final UserRepository userRepository;
    private final RecurrenceEditService recurrenceEditService;
    private final RecurrenceRuleRepository recurrenceRuleRepository;

    @Transactional
    public IncomeResponse create(Long userId, IncomeRequest request) {
        validate(request);
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        IncomeEntry entry = new IncomeEntry();
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
        return IncomeResponse.from(incomeEntryRepository.save(entry));
    }

    @Transactional
    public IncomeResponse update(Long userId, Long id, IncomeRequest request) {
        validate(request);
        IncomeEntry entry = getOwned(userId, id);
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
                        IncomeEntry next = new IncomeEntry();
                        next.setUser(entry.getUser());
                        applyFields(userId, next, request);
                        next.setRecurrenceRule(newRule);
                        next.setBaseRecurrence(true);
                        incomeEntryRepository.save(next);
                    }
            );
            if (scope == RecurrenceScope.THIS_ONLY) {
                return IncomeResponse.from(entry);
            }
        } else {
            applyFields(userId, entry, request);
        }
        return IncomeResponse.from(incomeEntryRepository.save(entry));
    }

    @Transactional
    public void delete(Long userId, Long id, RecurrenceScope scope) {
        IncomeEntry entry = getOwned(userId, id);
        if (entry.getRecurrenceRule() != null) {
            RecurrenceRule rule = entry.getRecurrenceRule();
            recurrenceEditService.handleDelete(rule, entry.getDate(), scope, () -> {
                incomeEntryRepository.findByUserIdAndRecurrenceRuleIdAndBaseRecurrenceTrue(userId, rule.getId())
                        .ifPresent(incomeEntryRepository::delete);
                recurrenceRuleRepository.delete(rule);
            });
            return;
        }
        incomeEntryRepository.delete(entry);
    }

    private void applyFields(Long userId, IncomeEntry entry, IncomeRequest request) {
        entry.setDate(request.getDate());
        entry.setAmount(request.getAmount());
        entry.setDescription(request.getDescription());
        if (request.getIncomeSourceId() != null) {
            IncomeSource source = incomeSourceRepository.findByIdAndUserId(request.getIncomeSourceId(), userId)
                    .orElseThrow(() -> new EntityNotFoundException("Fonte de receita não encontrada"));
            entry.setIncomeSource(source);
            entry.setSourceNameSnapshot(source.getName());
        } else {
            entry.setIncomeSource(null);
            if (entry.getSourceNameSnapshot() == null) {
                entry.setSourceNameSnapshot(null);
            }
        }
    }

    private void validate(IncomeRequest request) {
        if (request.getDate() == null) {
            throw new BusinessException("Data é obrigatória", "date");
        }
        if (request.getAmount() == null) {
            throw new BusinessException("Valor é obrigatório", "amount");
        }
    }

    private IncomeEntry getOwned(Long userId, Long id) {
        return incomeEntryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Entrada não encontrada"));
    }
}
