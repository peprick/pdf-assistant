package com.pdfassistant.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pdfassistant.backend.domain.DocumentChunk;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, UUID> {

	boolean existsByDocumentId(UUID documentId);

	@Query(value = """
			SELECT
				id AS "chunkId",
				page_number AS "pageNumber",
				chunk_index AS "chunkIndex",
				content AS "content",
				1 - (embedding <=> CAST(:embedding AS vector)) AS "score"
			FROM document_chunks
			WHERE document_id = :documentId
			ORDER BY embedding <=> CAST(:embedding AS vector)
			LIMIT :limit
			""", nativeQuery = true)
	List<DocumentChunkSearchResult> findNearestByDocumentId(
			@Param("documentId") UUID documentId,
			@Param("embedding") String embedding,
			@Param("limit") int limit);
}
