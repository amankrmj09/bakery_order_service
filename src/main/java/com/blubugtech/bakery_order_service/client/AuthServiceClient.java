package com.blubugtech.bakery_order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.blubugtech.bakery_order_service.dto.auth.TokenValidationRequestDto;
import com.blubugtech.bakery_order_service.dto.auth.TokenValidationResponseDto;

@FeignClient(name = "bakery-auth-service", path = "/api/auth")
public interface AuthServiceClient {

    @PostMapping("/validate-token")
    TokenValidationResponseDto validateToken(@RequestBody TokenValidationRequestDto request);

    @PostMapping("/validate")
    TokenValidationResponseDto validateTokenWithHeader(@RequestHeader("Authorization") String authHeader);

}
