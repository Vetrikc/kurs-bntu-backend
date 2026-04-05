package com.bntu.auth.service;

import com.bntu.auth.dto.AuthResponse;
import com.bntu.auth.entity.Role;
import com.bntu.auth.entity.User;
import com.bntu.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(String username, String password, String roleStr) {
        if (username == null || username.isBlank() || password == null || password.length() < 6)
            return AuthResponse.error("Логин не может быть пустым, пароль — минимум 6 символов");

        if (userRepository.findByUsername(username).isPresent())
            return AuthResponse.error("Пользователь '" + username + "' уже существует");

        Role role;
        try {
            role = (roleStr != null) ? Role.valueOf(roleStr.toUpperCase()) : Role.USER;
        } catch (IllegalArgumentException e) {
            return AuthResponse.error("Неизвестная роль: " + roleStr);
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
        userRepository.save(user);
        return AuthResponse.ok("Пользователь '" + username + "' зарегистрирован");
    }

    @Transactional
    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword()))
            return AuthResponse.error("Неверный логин или пароль");

        String accessToken  = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return AuthResponse.ok("Вход выполнен", accessToken, refreshToken,
                user.getUsername(), user.getRole().name());
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null || !jwtService.isValid(refreshToken))
            return AuthResponse.error("Refresh token невалиден или истёк");

        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);
        if (user == null)
            return AuthResponse.error("Refresh token не найден — войдите заново");

        String newAccess  = jwtService.generateAccessToken(user.getUsername(), user.getRole());
        String newRefresh = jwtService.generateRefreshToken(user.getUsername());
        user.setRefreshToken(newRefresh);
        userRepository.save(user);

        return AuthResponse.ok("Token обновлён", newAccess, newRefresh,
                user.getUsername(), user.getRole().name());
    }

    public AuthResponse validate(String token) {
        if (token == null || !jwtService.isValid(token))
            return AuthResponse.error("Token невалиден или истёк");

        Claims claims = jwtService.parse(token);
        String info = String.format("Пользователь: %s | Роль: %s | Истекает: %s",
                claims.getSubject(), claims.get("role"), claims.getExpiration());
        return AuthResponse.ok(info, null, null, claims.getSubject(),
                (String) claims.get("role"));
    }

    @Transactional
    public AuthResponse logout(String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken).orElse(null);
        if (user == null)
            return AuthResponse.error("Сессия не найдена");
        user.setRefreshToken(null);
        userRepository.save(user);
        return AuthResponse.ok("Выход выполнен");
    }
}