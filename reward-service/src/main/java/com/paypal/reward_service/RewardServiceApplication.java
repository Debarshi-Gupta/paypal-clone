package com.paypal.reward_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.paypal.reward_service.service.feign")
public class RewardServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RewardServiceApplication.class, args);
	}

}
