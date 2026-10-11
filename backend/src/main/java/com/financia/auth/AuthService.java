package com.financia.auth;

import com.financia.auth.dto.LoginResponse;
import com.financia.auth.dto.UserResponse;
import com.financia.common.BusinessException;
import com.financia.common.DuplicateEmailException;
import com.financia.user.ThemePreference;
import com.financia.user.User;
import com.financia.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public UserResponse register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("Email já cadastrado");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email.toLowerCase().trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setTokenVersion(1);
        user.setThemePreference(ThemePreference.LIGHT);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
        int version = user.getTokenVersion() != null ? user.getTokenVersion() : 1;
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId(), version);
        Instant expiresAt = Instant.now().plusMillis(jwtTokenProvider.getExpirationMs());
        return new LoginResponse(token, expiresAt);
    }

    @Transactional
    public void forgotPassword(String email, String newPassword, String confirmation) {
        if (newPassword == null || confirmation == null || !newPassword.equals(confirmation)) {
            throw new BusinessException("A confirmação deve ser igual à senha nova", "new_password_confirmation");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException("Senha deve ter no mínimo 6 caracteres", "new_password");
        }
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            int version = user.getTokenVersion() != null ? user.getTokenVersion() : 1;
            user.setTokenVersion(version + 1);
            userRepository.save(user);
        });
    }
}
