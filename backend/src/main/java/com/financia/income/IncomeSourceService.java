package com.financia.income;

import com.financia.income.dto.NamedItemResponse;
import com.financia.user.User;
import com.financia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeSourceService {

    private final IncomeSourceRepository repository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NamedItemResponse> listActive(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream()
                .map(source -> new NamedItemResponse(source.getId(), source.getName()))
                .toList();
    }

    @Transactional
    public NamedItemResponse create(Long userId, String name) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        IncomeSource source = new IncomeSource();
        source.setUser(user);
        source.setName(name);
        source.setActive(true);
        repository.save(source);
        return new NamedItemResponse(source.getId(), source.getName());
    }

    @Transactional
    public NamedItemResponse rename(Long userId, Long id, String name) {
        IncomeSource source = getOwned(userId, id);
        source.setName(name);
        return new NamedItemResponse(source.getId(), source.getName());
    }

    @Transactional
    public void softDelete(Long userId, Long id) {
        IncomeSource source = getOwned(userId, id);
        source.setActive(false);
    }

    public IncomeSource getOwned(Long userId, Long id) {
        IncomeSource source = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Fonte de receita não encontrada"));
        if (!source.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado");
        }
        return source;
    }
}
