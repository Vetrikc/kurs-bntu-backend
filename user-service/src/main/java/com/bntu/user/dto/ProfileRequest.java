package com.bntu.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProfileRequest {
    private String action;       // GET_PROFILE | CREATE_PROFILE | UPDATE_PROFILE | DELETE_PROFILE
    private String token;        // access JWT
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;    // "yyyy-MM-dd"
    private String bio;
}