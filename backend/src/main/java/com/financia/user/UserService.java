package com.financia.user;

import com.financia.auth.dto.UserResponse;
import com.financia.common.DuplicateEmailException;
import com.financia.user.dto.ChangePasswordRequest;
import com.financia.user.dto.UpdateUserRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return UserResponse.from(get(userId));
    }

    @Transactional
    public UserResponse update(Long userId, UpdateUserRequest request) {
        User user = get(userId);
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            String email = request.getEmail().toLowerCase().trim();
            if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
                throw new DuplicateEmailException("Email já cadastrado");
            }
            user.setEmail(email);
        }
        if (request.getThemePreference() != null) {
            user.setThemePreference(request.getThemePreference());
        }
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = get(userId);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Senha atual incorreta");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void delete(Long userId) {
        userRepository.delete(get(userId));
    }

    private User get(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));
    }
}
