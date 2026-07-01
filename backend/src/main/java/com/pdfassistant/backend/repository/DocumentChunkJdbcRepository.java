package com.pdfassistant.backend.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentChunkJdbcRepository {

	private static final String INSERT_SQL = """
			INSERT INTO document_chunks (id, document_id, page_number, chunk_index, content, embedding)
			VALUES (?, ?, ?, ?, ?, CAST(? AS vector))
			""";

	private final JdbcTemplate jdbcTemplate;

	public DocumentChunkJdbcRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void saveAll(List<ChunkInsert> chunks) {
		if (chunks.isEmpty()) {
			return;
		}
		jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ChunkInsert chunk = chunks.get(i);
				ps.setObject(1, chunk.id());
				ps.setObject(2, chunk.documentId());
				ps.setInt(3, chunk.pageNumber());
				ps.setInt(4, chunk.chunkIndex());
				ps.setString(5, chunk.content());
				ps.setString(6, chunk.embedding());
			}

			@Override
			public int getBatchSize() {
				return chunks.size();
			}
		});
	}

	public record ChunkInsert(UUID id, UUID documentId, int pageNumber, int chunkIndex, String content,
			String embedding) {
	}
}
