package com.pdfassistant.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.pdfassistant.backend.config.AppProperties;

@Component
public class OllamaClient {

	private final RestClient restClient;
	private final AppProperties properties;

	public OllamaClient(RestClient restClient, AppProperties properties) {
		this.restClient = restClient;
		this.properties = properties;
	}

	public List<Double> embed(String input) {
		List<List<Double>> embeddings = embed(List.of(input));
		if (embeddings.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama did not return an embedding");
		}
		return embeddings.get(0);
	}

	public List<List<Double>> embed(List<String> inputs) {
		if (inputs.isEmpty()) {
			return List.of();
		}
		try {
			EmbeddingResponse response = restClient.post()
					.uri("/api/embed")
					.body(new EmbeddingRequest(properties.getOllama().getEmbeddingModel(), inputs, true))
					.retrieve()
					.body(EmbeddingResponse.class);
			if (response == null || response.embeddings() == null || response.embeddings().isEmpty()) {
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama did not return embeddings");
			}
			return response.embeddings();
		}
		catch (RestClientException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Could not connect to Ollama at " + properties.getOllama().getBaseUrl(), ex);
		}
	}

	public String chat(List<OllamaMessage> messages) {
		try {
			ChatResponse response = restClient.post()
					.uri("/api/chat")
					.body(new ChatRequest(properties.getOllama().getChatModel(), messages, false, false))
					.retrieve()
					.body(ChatResponse.class);
			if (response == null || response.message() == null || response.message().content() == null) {
				throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Ollama did not return a response");
			}
			return response.message().content();
		}
		catch (RestClientException ex) {
			throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
					"Could not connect to Ollama at " + properties.getOllama().getBaseUrl(), ex);
		}
	}

	private record EmbeddingRequest(String model, Object input, boolean truncate) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record EmbeddingResponse(String model, List<List<Double>> embeddings) {
	}

	private record ChatRequest(String model, List<OllamaMessage> messages, boolean stream, boolean think) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record OllamaMessage(String role, String content) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record ChatResponse(String model, OllamaMessage message, boolean done) {
	}
}
