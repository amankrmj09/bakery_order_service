package com.shah_s.bakery_order_service.dto.auth;

import java.util.List;

public class TokenValidationResponseDto {
    private String userId;
    private String email;
    private List<String> roles;
    private boolean valid;

    public TokenValidationResponseDto() {}

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
