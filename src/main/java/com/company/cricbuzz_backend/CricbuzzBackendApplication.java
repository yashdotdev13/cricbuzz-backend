package com.company.cricbuzz_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CricbuzzBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CricbuzzBackendApplication.class, args);
	}

}
