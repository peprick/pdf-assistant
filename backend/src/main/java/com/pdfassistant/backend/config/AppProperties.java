package com.pdfassistant.backend.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

	private Storage storage = new Storage();
	private Ollama ollama = new Ollama();
	private Rag rag = new Rag();

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
}
