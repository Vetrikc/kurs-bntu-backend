package com.bntu.user.socket;

import com.bntu.user.dto.ProfileRequest;
import com.bntu.user.dto.ProfileResponse;
import com.bntu.user.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ProfileService profileService;
    private final ObjectMapper mapper;

    public ClientHandler(Socket socket, ProfileService profileService) {
        this.socket = socket;
        this.profileService = profileService;
        this.mapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void run() {
        log.info("+ User клиент: {}", socket.getRemoteSocketAddress());
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
            log.info("- User клиент отключился: {}", socket.getRemoteSocketAddress());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private ProfileResponse handle(String json) {
        try {
            ProfileRequest req = mapper.readValue(json, ProfileRequest.class);
            if (req.getAction() == null)
                return ProfileResponse.error("Поле 'action' обязательно");

            return switch (req.getAction().toUpperCase()) {
                case "GET_PROFILE"    -> profileService.getProfile(req.getToken());
                case "CREATE_PROFILE" -> profileService.createProfile(req.getToken(), req);
                case "UPDATE_PROFILE" -> profileService.updateProfile(req.getToken(), req);
                case "DELETE_PROFILE" -> profileService.deleteProfile(req.getToken());
                default -> ProfileResponse.error(
                        "Неизвестный action. Доступные: GET_PROFILE, CREATE_PROFILE, UPDATE_PROFILE, DELETE_PROFILE");
            };
        } catch (Exception e) {
            return ProfileResponse.error("Неверный JSON: " + e.getMessage());
        }
    }
}