package com.financia.paymentmethod;

import com.financia.common.BusinessException;
import com.financia.paymentmethod.dto.PaymentMethodRequest;
import com.financia.paymentmethod.dto.PaymentMethodResponse;
import com.financia.user.User;
import com.financia.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private final PaymentMethodRepository repository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> listActive(Long userId) {
        return repository.findByUserIdAndActiveTrue(userId).stream()
                .map(PaymentMethodResponse::from)
                .toList();
    }

    @Transactional
    public PaymentMethodResponse create(Long userId, PaymentMethodRequest request) {
        validateCreditCard(request);
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
        PaymentMethod method = new PaymentMethod();
        method.setUser(user);
        apply(method, request);
        return PaymentMethodResponse.from(repository.save(method));
    }

    @Transactional
    public PaymentMethodResponse update(Long userId, Long id, PaymentMethodRequest request) {
        validateCreditCard(request);
        PaymentMethod method = getOwned(userId, id);
        apply(method, request);
        return PaymentMethodResponse.from(method);
    }

    @Transactional
    public void softDelete(Long userId, Long id) {
        getOwned(userId, id).setActive(false);
    }

    public PaymentMethod getOwned(Long userId, Long id) {
        return repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Fonte de pagamento não encontrada"));
    }

    private void apply(PaymentMethod method, PaymentMethodRequest request) {
        method.setName(request.getName());
        method.setType(request.getType());
        method.setDueDay(request.getDueDay());
        method.setBillingStartDay(request.getBillingStartDay());
        method.setBillingEndDay(request.getBillingEndDay());
        method.setActive(true);
    }

    private void validateCreditCard(PaymentMethodRequest request) {
        if (request.getType() == PaymentMethodType.CREDIT_CARD) {
            if (request.getDueDay() == null || request.getBillingStartDay() == null || request.getBillingEndDay() == null) {
                throw new BusinessException(
                        "Cartão de crédito exige data de vencimento e período de faturamento",
                        "due_day"
                );
            }
        }
    }
}
