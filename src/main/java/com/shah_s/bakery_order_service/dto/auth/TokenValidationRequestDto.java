package com.shah_s.bakery_order_service.dto.auth;

public class TokenValidationRequestDto {
    private String token;

    public TokenValidationRequestDto() {}

    public TokenValidationRequestDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
