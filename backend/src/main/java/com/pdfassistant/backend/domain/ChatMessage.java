package com.pdfassistant.backend.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "document_id", nullable = false)
	private PdfDocument document;

	@Column(nullable = false)
	private String role;

	@Lob
	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	private Instant createdAt;

	protected ChatMessage() {
	}

	public ChatMessage(UUID id, PdfDocument document, String role, String content) {
		this.id = id;
		this.document = document;
		this.role = role;
		this.content = content;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public PdfDocument getDocument() {
		return document;
	}

	public String getRole() {
		return role;
	}

	public String getContent() {
		return content;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
