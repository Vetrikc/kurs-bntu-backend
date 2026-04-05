package com.bntu.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthRequest {
    private String action;        // REGISTER | LOGIN | REFRESH | VALIDATE | LOGOUT
    private String username;
    private String password;
    private String role;
    private String token;
    private String refreshToken;
}