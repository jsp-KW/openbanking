package com.fintech.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OpenbankingApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenbankingApiApplication.class, args);
	}

}
