package com.pdfassistant.backend.dto;

import java.util.UUID;

public record SourceResponse(
		UUID chunkId,
		int pageNumber,
		int chunkIndex,
		double score,
		String snippet) {
}
