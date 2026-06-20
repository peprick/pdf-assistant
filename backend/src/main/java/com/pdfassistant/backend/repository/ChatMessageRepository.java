package com.pdfassistant.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pdfassistant.backend.domain.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

	List<ChatMessage> findByDocumentIdOrderByCreatedAtAsc(UUID documentId);
}
