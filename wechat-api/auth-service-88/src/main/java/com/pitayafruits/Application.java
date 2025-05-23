package com.pitayafruits;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan(basePackages = "com.pitayafruits.mapper")
@EnableFeignClients("com.pitayafruits.api.feign")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
