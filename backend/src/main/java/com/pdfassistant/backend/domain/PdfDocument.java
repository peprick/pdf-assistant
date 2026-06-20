package com.pdfassistant.backend.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pdf_documents")
public class PdfDocument {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String fileName;

	@Column(nullable = false)
	private String storedFileName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private DocumentStatus status;

	private Integer pageCount;

	@Column(length = 2000)
	private String errorMessage;

	@Column(nullable = false)
	private Instant createdAt;

	protected PdfDocument() {
	}

	public PdfDocument(UUID id, String fileName, String storedFileName) {
		this.id = id;
		this.fileName = fileName;
		this.storedFileName = storedFileName;
		this.status = DocumentStatus.PROCESSING;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public String getStoredFileName() {
		return storedFileName;
	}

	public DocumentStatus getStatus() {
		return status;
	}

	public void setStatus(DocumentStatus status) {
		this.status = status;
	}

	public Integer getPageCount() {
		return pageCount;
	}

	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
