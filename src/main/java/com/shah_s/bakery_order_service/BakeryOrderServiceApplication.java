package com.shah_s.bakery_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.Import;
import org.devofblue.common.security.MethodSecurityConfig;
import org.devofblue.common.security.FeignClientInterceptor;
import org.devofblue.common.kafka.KafkaConfig;

@SpringBootApplication
@EnableDiscoveryClient
@Import({MethodSecurityConfig.class, FeignClientInterceptor.class, KafkaConfig.class, org.devofblue.common.feign.FeignConfig.class})
@EnableFeignClients
public class BakeryOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryOrderServiceApplication.class, args);
    }

}
