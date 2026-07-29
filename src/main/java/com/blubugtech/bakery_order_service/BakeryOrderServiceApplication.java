package com.blubugtech.bakery_order_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import org.springframework.context.annotation.Import;
import org.blubakery.bakery_common_libs.security.MethodSecurityConfig;
import org.blubakery.bakery_common_libs.security.FeignClientInterceptor;
import org.blubakery.bakery_common_libs.kafka.KafkaConfig;

@SpringBootApplication
@EnableDiscoveryClient
@Import({MethodSecurityConfig.class, FeignClientInterceptor.class, KafkaConfig.class, org.blubakery.bakery_common_libs.feign.FeignConfig.class})
@EnableFeignClients
public class BakeryOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BakeryOrderServiceApplication.class, args);
    }

}
