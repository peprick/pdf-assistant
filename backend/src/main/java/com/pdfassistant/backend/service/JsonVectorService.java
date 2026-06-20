package com.pdfassistant.backend.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonVectorService {

	private static final TypeReference<List<Double>> DOUBLE_LIST = new TypeReference<>() {
	};

	private final ObjectMapper objectMapper;

	public JsonVectorService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String toJson(List<Double> vector) {
		try {
			return objectMapper.writeValueAsString(vector);
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not serialize embedding vector", ex);
		}
	}

	public double[] fromJson(String json) {
		try {
			List<Double> values = objectMapper.readValue(json, DOUBLE_LIST);
			double[] vector = new double[values.size()];
			for (int i = 0; i < values.size(); i++) {
				vector[i] = values.get(i);
			}
			return vector;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Could not deserialize embedding vector", ex);
		}
	}
}
