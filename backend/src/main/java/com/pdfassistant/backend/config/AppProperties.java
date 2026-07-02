package com.pdfassistant.backend.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Storage storage = new Storage();
	private Ollama ollama = new Ollama();
	private Rag rag = new Rag();
	private Ocr ocr = new Ocr();

	public Storage getStorage() {
		return storage;
	}

	public void setStorage(Storage storage) {
		this.storage = storage;
	}

	public Ollama getOllama() {
		return ollama;
	}

	public void setOllama(Ollama ollama) {
		this.ollama = ollama;
	}

	public Rag getRag() {
		return rag;
	}

	public void setRag(Rag rag) {
		this.rag = rag;
	}

	public Ocr getOcr() {
		return ocr;
	}

	public void setOcr(Ocr ocr) {
		this.ocr = ocr;
	}

	public static class Storage {
		private Path uploadDir = Path.of("./data/uploads");

		public Path getUploadDir() {
			return uploadDir;
		}

		public void setUploadDir(Path uploadDir) {
			this.uploadDir = uploadDir;
		}
	}

	public static class Ollama {
		private String baseUrl = "http://localhost:11434";
		private String chatModel = "qwen3:8b";
		private String embeddingModel = "nomic-embed-text";

		public String getBaseUrl() {
			return baseUrl;
		}

		public void setBaseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
		}

		public String getChatModel() {
			return chatModel;
		}

		public void setChatModel(String chatModel) {
			this.chatModel = chatModel;
		}

		public String getEmbeddingModel() {
			return embeddingModel;
		}

		public void setEmbeddingModel(String embeddingModel) {
			this.embeddingModel = embeddingModel;
		}
	}

	public static class Rag {
		private int chunkSize = 2200;
		private int chunkOverlap = 250;
		private int maxResults = 5;
		private int embeddingDimensions = 768;

		public int getChunkSize() {
			return chunkSize;
		}

		public void setChunkSize(int chunkSize) {
			this.chunkSize = chunkSize;
		}

		public int getChunkOverlap() {
			return chunkOverlap;
		}

		public void setChunkOverlap(int chunkOverlap) {
			this.chunkOverlap = chunkOverlap;
		}

		public int getMaxResults() {
			return maxResults;
		}

		public void setMaxResults(int maxResults) {
			this.maxResults = maxResults;
		}

		public int getEmbeddingDimensions() {
			return embeddingDimensions;
		}

		public void setEmbeddingDimensions(int embeddingDimensions) {
			this.embeddingDimensions = embeddingDimensions;
		}
	}

	public static class Ocr {
		private boolean enabled = true;
		private String tesseractCommand = "tesseract";
		private String language = "eng";
		private int dpi = 300;
		private int pageSegmentationMode = 6;
		private int minTextCharactersPerPage = 40;
		private int timeoutSeconds = 60;

		public boolean isEnabled() {
			return enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getTesseractCommand() {
			return tesseractCommand;
		}

		public void setTesseractCommand(String tesseractCommand) {
			this.tesseractCommand = tesseractCommand;
		}

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public int getDpi() {
			return dpi;
		}

		public void setDpi(int dpi) {
			this.dpi = dpi;
		}

		public int getPageSegmentationMode() {
			return pageSegmentationMode;
		}

		public void setPageSegmentationMode(int pageSegmentationMode) {
			this.pageSegmentationMode = pageSegmentationMode;
		}

		public int getMinTextCharactersPerPage() {
			return minTextCharactersPerPage;
		}

		public void setMinTextCharactersPerPage(int minTextCharactersPerPage) {
			this.minTextCharactersPerPage = minTextCharactersPerPage;
		}

		public int getTimeoutSeconds() {
			return timeoutSeconds;
		}

		public void setTimeoutSeconds(int timeoutSeconds) {
			this.timeoutSeconds = timeoutSeconds;
		}
	}
}
