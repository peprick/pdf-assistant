package com.pdfassistant.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pdfassistant.backend.domain.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

	List<DocumentChunk> findByDocumentIdOrderByChunkIndex(UUID documentId);
}
