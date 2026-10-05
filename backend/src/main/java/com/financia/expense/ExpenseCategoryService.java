package com.financia.expense;

import com.financia.income.dto.NamedItemResponse;
import com.financia.user.User;
import com.financia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseCategoryService {

    private final ExpenseCategoryRepository repository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NamedItemResponse> listActive(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream()
                .map(category -> new NamedItemResponse(category.getId(), category.getName()))
                .toList();
    }

    @Transactional
    public NamedItemResponse create(Long userId, String name) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        ExpenseCategory category = new ExpenseCategory();
        category.setUser(user);
        category.setName(name);
        category.setActive(true);
        repository.save(category);
        return new NamedItemResponse(category.getId(), category.getName());
    }

    @Transactional
    public NamedItemResponse rename(Long userId, Long id, String name) {
        ExpenseCategory category = getOwned(userId, id);
        category.setName(name);
        return new NamedItemResponse(category.getId(), category.getName());
    }

    @Transactional
    public void softDelete(Long userId, Long id) {
        getOwned(userId, id).setActive(false);
    }

    public ExpenseCategory getOwned(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }
}
