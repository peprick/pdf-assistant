package com.pdfassistant.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pdfassistant.backend.config.AppProperties;
import com.pdfassistant.backend.domain.ChatMessage;
import com.pdfassistant.backend.domain.DocumentStatus;
import com.pdfassistant.backend.domain.PdfDocument;
import com.pdfassistant.backend.dto.AskQuestionResponse;
import com.pdfassistant.backend.dto.SourceResponse;
import com.pdfassistant.backend.repository.ChatMessageRepository;
import com.pdfassistant.backend.repository.DocumentChunkRepository;
import com.pdfassistant.backend.repository.DocumentChunkSearchResult;
import com.pdfassistant.backend.repository.PdfDocumentRepository;
import com.pdfassistant.backend.service.OllamaClient.OllamaMessage;

@Service
public class RagService {

	private static final String SYSTEM_PROMPT = """
			You are a careful PDF assistant.
			Answer only from the provided PDF context.
			If the answer is not present in the context, say you cannot find it in the PDF.
			Cite page numbers for factual claims using the format [p. 3].
			Keep the answer clear and useful.
			""";

	private final AppProperties properties;
	private final PdfDocumentRepository documentRepository;
	private final DocumentChunkRepository chunkRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final OllamaClient ollamaClient;
	private final PgVectorService pgVectorService;

	public RagService(AppProperties properties, PdfDocumentRepository documentRepository,
			DocumentChunkRepository chunkRepository, ChatMessageRepository chatMessageRepository, OllamaClient ollamaClient,
			PgVectorService pgVectorService) {
		this.properties = properties;
		this.documentRepository = documentRepository;
		this.chunkRepository = chunkRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.ollamaClient = ollamaClient;
		this.pgVectorService = pgVectorService;
	}

	@Transactional
	public AskQuestionResponse ask(UUID documentId, String question) {
		PdfDocument document = documentRepository.findById(documentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
		if (document.getStatus() != DocumentStatus.READY) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document is not ready for questions");
		}

		if (!chunkRepository.existsByDocumentId(documentId)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Document has no searchable chunks");
		}

		String questionEmbedding = pgVectorService.toLiteral(ollamaClient.embed(question),
				properties.getRag().getEmbeddingDimensions());
		List<DocumentChunkSearchResult> topChunks = chunkRepository.findNearestByDocumentId(
				documentId,
				questionEmbedding,
				properties.getRag().getMaxResults());

		chatMessageRepository.save(new ChatMessage(UUID.randomUUID(), document, "user", question));
		String answer = cleanAnswer(ollamaClient.chat(List.of(
				new OllamaMessage("system", SYSTEM_PROMPT),
				new OllamaMessage("user", buildPrompt(question, topChunks)))));
		chatMessageRepository.save(new ChatMessage(UUID.randomUUID(), document, "assistant", answer));

		return new AskQuestionResponse(answer, topChunks.stream().map(this::toSource).toList());
	}

	private String buildPrompt(String question, List<DocumentChunkSearchResult> topChunks) {
		StringBuilder context = new StringBuilder();
		for (int i = 0; i < topChunks.size(); i++) {
			DocumentChunkSearchResult chunk = topChunks.get(i);
			context.append("Source ")
					.append(i + 1)
					.append(" | page ")
					.append(chunk.getPageNumber())
					.append(" | chunk ")
					.append(chunk.getChunkIndex())
					.append(":\n")
					.append(chunk.getContent())
					.append("\n\n");
		}
		return """
				Question:
				%s

				PDF context:
				%s
				""".formatted(question, context);
	}

	private SourceResponse toSource(DocumentChunkSearchResult chunk) {
		return new SourceResponse(
				chunk.getChunkId(),
				chunk.getPageNumber(),
				chunk.getChunkIndex(),
				round(chunk.getScore()),
				snippet(chunk.getContent()));
	}

	private String snippet(String content) {
		String normalized = content.replaceAll("\\s+", " ").trim();
		if (normalized.length() <= 240) {
			return normalized;
		}
		return normalized.substring(0, 237) + "...";
	}

	private String cleanAnswer(String answer) {
		return answer.replaceAll("(?s)<think>.*?</think>", "").trim();
	}

	private double round(double value) {
		return Math.round(value * 10000.0) / 10000.0;
	}
}
