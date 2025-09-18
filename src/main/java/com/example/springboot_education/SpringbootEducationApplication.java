package com.example.springboot_education;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootEducationApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootEducationApplication.class, args);
	}

}
