CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE pdf_documents (
	id UUID PRIMARY KEY,
	file_name VARCHAR(255) NOT NULL,
	stored_file_name VARCHAR(255) NOT NULL,
	status VARCHAR(32) NOT NULL,
	page_count INTEGER,
	error_message VARCHAR(2000),
	created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE document_chunks (
	id UUID PRIMARY KEY,
	document_id UUID NOT NULL REFERENCES pdf_documents(id) ON DELETE CASCADE,
	page_number INTEGER NOT NULL,
	chunk_index INTEGER NOT NULL,
	content TEXT NOT NULL,
	embedding vector(768) NOT NULL
);

CREATE INDEX idx_document_chunks_document_chunk
	ON document_chunks (document_id, chunk_index);

CREATE INDEX idx_document_chunks_embedding_hnsw
	ON document_chunks USING hnsw (embedding vector_cosine_ops);

CREATE TABLE chat_messages (
	id UUID PRIMARY KEY,
	document_id UUID NOT NULL REFERENCES pdf_documents(id) ON DELETE CASCADE,
	role VARCHAR(255) NOT NULL,
	content TEXT NOT NULL,
	created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_chat_messages_document_created
	ON chat_messages (document_id, created_at);
