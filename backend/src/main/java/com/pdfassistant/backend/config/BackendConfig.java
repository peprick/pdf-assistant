package com.pdfassistant.backend.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class BackendConfig {

	@Bean
	RestClient ollamaRestClient(AppProperties properties) {
		return RestClient.builder()
				.baseUrl(properties.getOllama().getBaseUrl())
				.build();
	}

	@Bean
	CommandLineRunner createStorageDirectories(AppProperties properties) {
		return args -> createDirectories(properties);
	}

	private void createDirectories(AppProperties properties) throws IOException {
		Files.createDirectories(properties.getStorage().getUploadDir().toAbsolutePath().normalize());
		Files.createDirectories(Path.of("./data/h2").toAbsolutePath().normalize());
	}
}
