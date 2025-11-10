package com.qbit.framework.business.openapi.auth.starter.model;

import lombok.Data;

@Data
public class AccessTokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Long timestamp;
}