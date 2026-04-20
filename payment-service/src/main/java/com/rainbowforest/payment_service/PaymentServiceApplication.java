package com.rainbowforest.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan; // Thêm dòng này

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = { "com.rainbowforest.payment_service" }) // Ép quét đúng package này
public class PaymentServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaymentServiceApplication.class, args);
	}
}