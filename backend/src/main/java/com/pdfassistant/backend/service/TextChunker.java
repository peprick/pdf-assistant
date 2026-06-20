package com.pdfassistant.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TextChunker {

	public List<String> chunk(String text, int chunkSize, int overlap) {
		String normalized = normalize(text);
		if (normalized.isBlank()) {
			return List.of();
		}

		List<String> chunks = new ArrayList<>();
		int start = 0;
		while (start < normalized.length()) {
			int end = Math.min(normalized.length(), start + chunkSize);
			int adjustedEnd = adjustEnd(normalized, start, end);
			String chunk = normalized.substring(start, adjustedEnd).trim();
			if (!chunk.isBlank()) {
				chunks.add(chunk);
			}
			if (adjustedEnd >= normalized.length()) {
				break;
			}
			int safeOverlap = Math.min(overlap, Math.max(0, adjustedEnd - start - 1));
			start = Math.max(0, adjustedEnd - safeOverlap);
			while (start < normalized.length() && Character.isWhitespace(normalized.charAt(start))) {
				start++;
			}
		}
		return chunks;
	}

	private String normalize(String text) {
		return text == null ? "" : text
				.replace("\r", "\n")
				.replaceAll("[ \\t]+", " ")
				.replaceAll("\\n{3,}", "\n\n")
				.trim();
	}

	private int adjustEnd(String text, int start, int end) {
		if (end >= text.length()) {
			return text.length();
		}
		int searchFloor = Math.max(start + 1, end - 300);
		for (int i = end; i >= searchFloor; i--) {
			if (Character.isWhitespace(text.charAt(i - 1))) {
				return i;
			}
		}
		return end;
	}
}
