package com.bntu.auth.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private boolean success;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;

    public static AuthResponse ok(String msg) {
        return AuthResponse.builder().success(true).message(msg).build();
    }

    public static AuthResponse ok(String msg, String access, String refresh,
                                   String username, String role) {
        return AuthResponse.builder()
                .success(true).message(msg)
                .accessToken(access).refreshToken(refresh)
                .username(username).role(role)
                .build();
    }

    public static AuthResponse error(String msg) {
        return AuthResponse.builder().success(false).message(msg).build();
    }
}