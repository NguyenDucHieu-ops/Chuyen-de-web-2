package com.rainbowforest.reward_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class RewardServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(RewardServiceApplication.class, args);
	}
}