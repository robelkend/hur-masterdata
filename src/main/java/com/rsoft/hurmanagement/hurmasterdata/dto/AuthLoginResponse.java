package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

@Data
public class AuthLoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private AuthUserDTO user;
    private boolean passwordExpired;
}
