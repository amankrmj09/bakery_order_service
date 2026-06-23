package com.shah_s.bakery_order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "bakery-auth-service", path = "/api/auth")
public interface AuthServiceClient {

    @PostMapping("/validate-token")
    Map<String, Object> validateToken(@RequestBody Map<String, String> request);

    @PostMapping("/validate")
    Map<String, Object> validateTokenWithHeader(@RequestHeader("Authorization") String authHeader);

}
