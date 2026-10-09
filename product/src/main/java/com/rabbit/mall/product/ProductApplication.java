package com.rabbit.mall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class ProductApplication {
    @Bean public RestTemplate restTemplate() { return new RestTemplate(); }
    public static void main(String[] args) { SpringApplication.run(ProductApplication.class, args); }
}
