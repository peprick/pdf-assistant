package com.pdfassistant.backend.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private PdfDocument document;

	@Column(nullable = false)
	private int pageNumber;

	@Column(nullable = false)
	private int chunkIndex;

	@Lob
	@Column(nullable = false)
	private String content;

	@Lob
	@Column(nullable = false)
	private String embeddingJson;

	protected DocumentChunk() {
	}

	public DocumentChunk(UUID id, PdfDocument document, int pageNumber, int chunkIndex, String content,
			String embeddingJson) {
		this.id = id;
		this.document = document;
		this.pageNumber = pageNumber;
		this.chunkIndex = chunkIndex;
		this.content = content;
		this.embeddingJson = embeddingJson;
	}

	public UUID getId() {
		return id;
	}

	public PdfDocument getDocument() {
		return document;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getChunkIndex() {
		return chunkIndex;
	}

	public String getContent() {
		return content;
	}

	public String getEmbeddingJson() {
		return embeddingJson;
	}
}
