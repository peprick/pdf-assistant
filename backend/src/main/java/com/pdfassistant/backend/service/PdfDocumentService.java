package com.pdfassistant.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.pdfassistant.backend.config.AppProperties;
import com.pdfassistant.backend.domain.DocumentChunk;
import com.pdfassistant.backend.domain.DocumentStatus;
import com.pdfassistant.backend.domain.PdfDocument;
import com.pdfassistant.backend.dto.ChatMessageResponse;
import com.pdfassistant.backend.dto.DocumentResponse;
import com.pdfassistant.backend.repository.ChatMessageRepository;
import com.pdfassistant.backend.repository.DocumentChunkRepository;
import com.pdfassistant.backend.repository.PdfDocumentRepository;

@Service
public class PdfDocumentService {

	private static final int EMBEDDING_BATCH_SIZE = 16;

	private final AppProperties properties;
	private final PdfDocumentRepository documentRepository;
	private final DocumentChunkRepository chunkRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final TextChunker textChunker;
	private final OllamaClient ollamaClient;
	private final JsonVectorService jsonVectorService;

	public PdfDocumentService(AppProperties properties, PdfDocumentRepository documentRepository,
			DocumentChunkRepository chunkRepository, ChatMessageRepository chatMessageRepository, TextChunker textChunker,
			OllamaClient ollamaClient, JsonVectorService jsonVectorService) {
		this.properties = properties;
		this.documentRepository = documentRepository;
		this.chunkRepository = chunkRepository;
		this.chatMessageRepository = chatMessageRepository;
		this.textChunker = textChunker;
		this.ollamaClient = ollamaClient;
		this.jsonVectorService = jsonVectorService;
	}

	public DocumentResponse upload(MultipartFile file) {
		validatePdf(file);

		UUID documentId = UUID.randomUUID();
		String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
		String storedFileName = documentId + ".pdf";
		Path uploadPath = properties.getStorage().getUploadDir().toAbsolutePath().normalize();
		Path targetPath = uploadPath.resolve(storedFileName);

		PdfDocument document = new PdfDocument(documentId, originalFileName, storedFileName);
		documentRepository.save(document);

		try {
			Files.createDirectories(uploadPath);
			Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
			ingest(document, targetPath);
			return DocumentResponse.from(documentRepository.save(document));
		}
		catch (Exception ex) {
			document.setStatus(DocumentStatus.FAILED);
			document.setErrorMessage(limit(ex.getMessage(), 1800));
			documentRepository.save(document);
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not process PDF: " + ex.getMessage(), ex);
		}
	}

	@Transactional(readOnly = true)
	public List<DocumentResponse> listDocuments() {
		return documentRepository.findAllByOrderByCreatedAtDesc()
				.stream()
				.map(DocumentResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public DocumentResponse getDocument(UUID documentId) {
		return DocumentResponse.from(findDocument(documentId));
	}

	@Transactional(readOnly = true)
	public List<ChatMessageResponse> listMessages(UUID documentId) {
		findDocument(documentId);
		return chatMessageRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
				.stream()
				.map(ChatMessageResponse::from)
				.toList();
	}

	public PdfDocument findDocument(UUID documentId) {
		return documentRepository.findById(documentId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));
	}

	private void validatePdf(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a non-empty PDF file");
		}
		String fileName = file.getOriginalFilename();
		if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are supported");
		}
	}

	private void ingest(PdfDocument document, Path pdfPath) throws IOException {
		ExtractionResult extraction = extractChunks(pdfPath);
		document.setPageCount(extraction.pageCount());
		if (extraction.chunks().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"No extractable text was found. Scanned PDFs will need OCR support.");
		}

		int chunkIndex = 0;
		for (int start = 0; start < extraction.chunks().size(); start += EMBEDDING_BATCH_SIZE) {
			List<ChunkDraft> batch = extraction.chunks()
					.subList(start, Math.min(extraction.chunks().size(), start + EMBEDDING_BATCH_SIZE));
			List<String> textBatch = batch.stream().map(ChunkDraft::content).toList();
			List<List<Double>> embeddings = ollamaClient.embed(textBatch);
			if (embeddings.size() != batch.size()) {
				throw new IllegalStateException("Ollama returned a different embedding count than requested");
			}
			List<DocumentChunk> chunks = new ArrayList<>();
			for (int i = 0; i < batch.size(); i++) {
				ChunkDraft draft = batch.get(i);
				chunks.add(new DocumentChunk(
						UUID.randomUUID(),
						document,
						draft.pageNumber(),
						chunkIndex++,
						draft.content(),
						jsonVectorService.toJson(embeddings.get(i))));
			}
			chunkRepository.saveAll(chunks);
		}

		document.setStatus(DocumentStatus.READY);
	}

	private ExtractionResult extractChunks(Path pdfPath) throws IOException {
		List<ChunkDraft> chunks = new ArrayList<>();
		try (PDDocument pdf = Loader.loadPDF(pdfPath.toFile())) {
			PDFTextStripper stripper = new PDFTextStripper();
			for (int page = 1; page <= pdf.getNumberOfPages(); page++) {
				stripper.setStartPage(page);
				stripper.setEndPage(page);
				String pageText = stripper.getText(pdf);
				for (String chunk : textChunker.chunk(pageText, properties.getRag().getChunkSize(),
						properties.getRag().getChunkOverlap())) {
					chunks.add(new ChunkDraft(page, chunk));
				}
			}
			return new ExtractionResult(pdf.getNumberOfPages(), chunks);
		}
	}

	private String limit(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}

	private record ChunkDraft(int pageNumber, String content) {
	}

	private record ExtractionResult(int pageCount, List<ChunkDraft> chunks) {
	}
}
