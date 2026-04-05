package com.bntu.auth.socket;

import com.bntu.auth.dto.AuthRequest;
import com.bntu.auth.dto.AuthResponse;
import com.bntu.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final AuthService authService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ClientHandler(Socket socket, AuthService authService) {
        this.socket = socket;
        this.authService = authService;
    }

    @Override
    public void run() {
        log.info(" + Auth клиент: {}", socket.getRemoteSocketAddress());
        try (
            BufferedReader in  = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(
                    socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.isBlank()) continue;
                out.println(mapper.writeValueAsString(handle(line)));
            }
        } catch (IOException e) {
            log.info(" - Auth клиент отключился: {}", socket.getRemoteSocketAddress());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private AuthResponse handle(String json) {
        try {
            AuthRequest req = mapper.readValue(json, AuthRequest.class);
            if (req.getAction() == null)
                return AuthResponse.error("Поле 'action' обязательно");

            return switch (req.getAction().toUpperCase()) {
                case "REGISTER" -> authService.register(
                        req.getUsername(), req.getPassword(), req.getRole());
                case "LOGIN"    -> authService.login(
                        req.getUsername(), req.getPassword());
                case "REFRESH"  -> authService.refresh(req.getRefreshToken());
                case "VALIDATE" -> authService.validate(req.getToken());
                case "LOGOUT"   -> authService.logout(req.getRefreshToken());
                default         -> AuthResponse.error(
                        "Неизвестный action. Доступные: REGISTER, LOGIN, REFRESH, VALIDATE, LOGOUT");
            };
        } catch (Exception e) {
            return AuthResponse.error("Неверный JSON: " + e.getMessage());
        }
    }
}