package com.rainbowforest.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource; // 👈 Nhớ import cái này
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
// 🔥 ÉP NÓ ĐỌC ĐÚNG FILE TRONG THƯ MỤC RESOURCES
@PropertySource("classpath:application.properties")
@EnableEurekaClient
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}