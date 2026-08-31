package com.umaso.mantenimientos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MantenimientosApplication {

	public static void main(String[] args) {
		SpringApplication.run(MantenimientosApplication.class, args);
	}

}
