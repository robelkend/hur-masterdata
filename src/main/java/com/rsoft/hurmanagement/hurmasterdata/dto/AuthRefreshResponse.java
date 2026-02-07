package com.rsoft.hurmanagement.hurmasterdata.dto;

import lombok.Data;

@Data
public class AuthRefreshResponse {
    private String accessToken;
    private long expiresIn;
    private boolean passwordExpired;
}
