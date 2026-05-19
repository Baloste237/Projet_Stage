package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public org.springframework.boot.CommandLineRunner roleMigrationRunner(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				int updated = jdbcTemplate.update("UPDATE users SET role = 'ROLE_ANALYSTE_SECURITE' WHERE role = 'ROLE_ANALYSTE'");
				if (updated > 0) {
					System.out.println("Migrated " + updated + " users from ROLE_ANALYSTE to ROLE_ANALYSTE_SECURITE");
				}
			} catch (Exception e) {
				System.out.println("Role migration skipped or failed: " + e.getMessage());
			}
		};
	}
}


