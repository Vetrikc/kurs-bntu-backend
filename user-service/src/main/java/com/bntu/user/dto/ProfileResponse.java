package com.bntu.user.dto;

import com.bntu.user.entity.UserProfile;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private boolean success;
    private String message;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;
    private String bio;
    private String createdAt;
    private String updatedAt;

    public static ProfileResponse ok(String msg) {
        return ProfileResponse.builder().success(true).message(msg).build();
    }

    public static ProfileResponse error(String msg) {
        return ProfileResponse.builder().success(false).message(msg).build();
    }

    public static ProfileResponse fromProfile(String msg, UserProfile p) {
        return ProfileResponse.builder()
                .success(true).message(msg)
                .username(p.getUsername())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .birthDate(p.getBirthDate() != null ? p.getBirthDate().toString() : null)
                .bio(p.getBio())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .updatedAt(p.getUpdatedAt() != null ? p.getUpdatedAt().toString() : null)
                .build();
    }
}