package com.pdfassistant.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.pdfassistant.backend.dto.AskQuestionRequest;
import com.pdfassistant.backend.dto.AskQuestionResponse;
import com.pdfassistant.backend.dto.ChatMessageResponse;
import com.pdfassistant.backend.dto.DocumentResponse;
import com.pdfassistant.backend.service.PdfDocumentService;
import com.pdfassistant.backend.service.RagService;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

	private final PdfDocumentService documentService;
	private final RagService ragService;

	public DocumentController(PdfDocumentService documentService, RagService ragService) {
		this.documentService = documentService;
		this.ragService = ragService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DocumentResponse upload(@RequestParam("file") MultipartFile file) {
		return documentService.upload(file);
	}

	@GetMapping
	public List<DocumentResponse> listDocuments() {
		return documentService.listDocuments();
	}

	@GetMapping("/{documentId}")
	public DocumentResponse getDocument(@PathVariable UUID documentId) {
		return documentService.getDocument(documentId);
	}

	@GetMapping("/{documentId}/messages")
	public List<ChatMessageResponse> listMessages(@PathVariable UUID documentId) {
		return documentService.listMessages(documentId);
	}

	@PostMapping("/{documentId}/questions")
	public AskQuestionResponse ask(@PathVariable UUID documentId, @Valid @RequestBody AskQuestionRequest request) {
		return ragService.ask(documentId, request.question());
	}
}
