package com.pdfassistant.backend.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import com.pdfassistant.backend.config.AppProperties;

@Service
public class OcrService {

	private final AppProperties properties;

	public OcrService(AppProperties properties) {
		this.properties = properties;
	}

	public boolean isEnabled() {
		return properties.getOcr().isEnabled();
	}

	public String extractPageText(PDFRenderer renderer, int pageIndex) throws IOException {
		Path tempDir = Files.createTempDirectory("pdf-ocr-");
		Path imagePath = tempDir.resolve("page-" + (pageIndex + 1) + ".png");
		Path errorPath = tempDir.resolve("tesseract.err");
		try {
			BufferedImage image = renderer.renderImageWithDPI(pageIndex, properties.getOcr().getDpi(), ImageType.RGB);
			ImageIO.write(image, "png", imagePath.toFile());

			Process process = new ProcessBuilder(List.of(
					properties.getOcr().getTesseractCommand(),
					imagePath.toString(),
					"stdout",
					"-l",
					properties.getOcr().getLanguage(),
					"--psm",
					Integer.toString(properties.getOcr().getPageSegmentationMode())))
				.redirectError(errorPath.toFile())
				.start();

			boolean completed = waitFor(process);
			if (!completed) {
				process.destroyForcibly();
				process.waitFor(5, TimeUnit.SECONDS);
				throw new IOException("OCR timed out after " + properties.getOcr().getTimeoutSeconds() + " seconds");
			}
			String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			String error = Files.exists(errorPath) ? Files.readString(errorPath, StandardCharsets.UTF_8).trim() : "";
			if (process.exitValue() != 0) {
				throw new IOException("OCR failed: " + (error.isBlank() ? "tesseract exited with code "
						+ process.exitValue() : error));
			}
			return output;
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IOException("OCR was interrupted", ex);
		}
		finally {
			Files.deleteIfExists(imagePath);
			Files.deleteIfExists(errorPath);
			Files.deleteIfExists(tempDir);
		}
	}

	private boolean waitFor(Process process) throws InterruptedException {
		return process.waitFor(properties.getOcr().getTimeoutSeconds(), TimeUnit.SECONDS);
	}
}
