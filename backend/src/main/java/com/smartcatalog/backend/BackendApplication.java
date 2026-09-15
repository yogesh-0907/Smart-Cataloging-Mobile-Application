package com.smartcatalog.backend;

import com.smartcatalog.backend.entity.Artisan;
import com.smartcatalog.backend.repository.ArtisanRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	public CommandLineRunner ensureDefaultArtisan(ArtisanRepository artisanRepository) {
		return args -> {
			if (artisanRepository.count() > 0) {
				return;
			}

			Artisan artisan = new Artisan(
					"Demo Artisan",
					"Hyderabad",
					"9999999999",
					32,
					"10 years"
			);
			artisanRepository.save(artisan);
		};
	}
}
