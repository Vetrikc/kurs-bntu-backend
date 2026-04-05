package com.bntu.user.service;

import com.bntu.user.dto.ProfileRequest;
import com.bntu.user.dto.ProfileResponse;
import com.bntu.user.entity.UserProfile;
import com.bntu.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final UserProfileRepository profileRepository;
    private final JwtValidator jwtValidator;

    public ProfileResponse getProfile(String token) {
        String username = jwtValidator.extractUsername(token);
        if (username == null) return ProfileResponse.error("Невалидный токен");

        return profileRepository.findByUsername(username)
                .map(p -> ProfileResponse.fromProfile("Профиль получен", p))
                .orElse(ProfileResponse.error("Профиль не найден. Используй CREATE_PROFILE"));
    }

    @Transactional
    public ProfileResponse createProfile(String token, ProfileRequest req) {
        String username = jwtValidator.extractUsername(token);
        if (username == null) return ProfileResponse.error("Невалидный токен");

        if (profileRepository.findByUsername(username).isPresent())
            return ProfileResponse.error("Профиль уже существует. Используй UPDATE_PROFILE");

        if (req.getEmail() != null && profileRepository.existsByEmail(req.getEmail()))
            return ProfileResponse.error("Email уже занят");

        UserProfile profile = UserProfile.builder()
                .username(username)
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .birthDate(parseDate(req.getBirthDate()))
                .bio(req.getBio())
                .build();

        profileRepository.save(profile);
        return ProfileResponse.fromProfile("Профиль создан", profile);
    }

    @Transactional
    public ProfileResponse updateProfile(String token, ProfileRequest req) {
        String username = jwtValidator.extractUsername(token);
        if (username == null) return ProfileResponse.error("Невалидный токен");

        UserProfile profile = profileRepository.findByUsername(username)
                .orElse(null);
        if (profile == null)
            return ProfileResponse.error("Профиль не найден. Сначала создай профиль");

        if (req.getFirstName() != null) profile.setFirstName(req.getFirstName());
        if (req.getLastName()  != null) profile.setLastName(req.getLastName());
        if (req.getPhone()     != null) profile.setPhone(req.getPhone());
        if (req.getBio()       != null) profile.setBio(req.getBio());
        if (req.getBirthDate() != null) profile.setBirthDate(parseDate(req.getBirthDate()));

        if (req.getEmail() != null) {
            if (!req.getEmail().equals(profile.getEmail())
                    && profileRepository.existsByEmail(req.getEmail()))
                return ProfileResponse.error("Email уже занят");
            profile.setEmail(req.getEmail());
        }

        profileRepository.save(profile);
        return ProfileResponse.fromProfile("Профиль обновлён", profile);
    }

    @Transactional
    public ProfileResponse deleteProfile(String token) {
        String username = jwtValidator.extractUsername(token);
        if (username == null) return ProfileResponse.error("Невалидный токен");

        UserProfile profile = profileRepository.findByUsername(username)
                .orElse(null);
        if (profile == null) return ProfileResponse.error("Профиль не найден");

        profileRepository.delete(profile);
        return ProfileResponse.ok("Профиль удалён");
    }

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()) return null;
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            log.warn("Неверный формат даты: {}", date);
            return null;
        }
    }
}