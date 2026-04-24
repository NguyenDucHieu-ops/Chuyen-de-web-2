package com.rainbowforest.minigame_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class MinigameServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MinigameServiceApplication.class, args);
	}
}