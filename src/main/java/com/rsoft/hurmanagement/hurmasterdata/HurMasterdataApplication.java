package com.rsoft.hurmanagement.hurmasterdata;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HurMasterdataApplication {

	public static void main(String[] args) {
		SpringApplication.run(HurMasterdataApplication.class, args);
	}

}
