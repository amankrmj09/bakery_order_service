package com.blubugtech.bakery_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.Import;
import com.blubugtech.common.security.MethodSecurityConfig;
import com.blubugtech.common.security.FeignClientInterceptor;
import com.blubugtech.common.kafka.KafkaConfig;

@SpringBootApplication
@EnableDiscoveryClient
@Import({MethodSecurityConfig.class, FeignClientInterceptor.class, KafkaConfig.class, com.blubugtech.common.feign.FeignConfig.class})
@EnableFeignClients
public class BakeryOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryOrderServiceApplication.class, args);
    }

}
