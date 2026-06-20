package com.pdfassistant.backend.dto;

import java.time.Instant;
import java.util.UUID;

import com.pdfassistant.backend.domain.DocumentStatus;
import com.pdfassistant.backend.domain.PdfDocument;

public record DocumentResponse(
		UUID id,
		String fileName,
		DocumentStatus status,
		Integer pageCount,
		String errorMessage,
		Instant createdAt) {

	public static DocumentResponse from(PdfDocument document) {
		return new DocumentResponse(
				document.getId(),
				document.getFileName(),
				document.getStatus(),
				document.getPageCount(),
				document.getErrorMessage(),
				document.getCreatedAt());
	}
}
