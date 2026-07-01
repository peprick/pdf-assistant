package com.pdfassistant.backend.repository;

import java.util.UUID;

public interface DocumentChunkSearchResult {

	UUID getChunkId();

	int getPageNumber();

	int getChunkIndex();

	String getContent();

	double getScore();
}
