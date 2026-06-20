package com.pdfassistant.backend.dto;

import java.time.Instant;
import java.util.UUID;

import com.pdfassistant.backend.domain.ChatMessage;

public record ChatMessageResponse(UUID id, String role, String content, Instant createdAt) {

	public static ChatMessageResponse from(ChatMessage message) {
		return new ChatMessageResponse(
				message.getId(),
				message.getRole(),
				message.getContent(),
				message.getCreatedAt());
	}
}
