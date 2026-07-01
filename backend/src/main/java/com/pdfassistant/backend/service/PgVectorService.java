package com.pdfassistant.backend.service;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PgVectorService {

	public String toLiteral(List<Double> vector, int expectedDimensions) {
		if (vector.size() != expectedDimensions) {
			throw new IllegalArgumentException(
					"Expected embedding dimension " + expectedDimensions + " but received " + vector.size());
		}

		StringBuilder literal = new StringBuilder(vector.size() * 10);
		literal.append('[');
		for (int i = 0; i < vector.size(); i++) {
			Double value = vector.get(i);
			if (value == null || !Double.isFinite(value)) {
				throw new IllegalArgumentException("Embedding contains a non-finite value at index " + i);
			}
			if (i > 0) {
				literal.append(',');
			}
			literal.append(value);
		}
		literal.append(']');
		return literal.toString();
	}
}
