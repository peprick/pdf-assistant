package com.pdfassistant.backend.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pdfassistant.backend.domain.PdfDocument;

public interface PdfDocumentRepository extends JpaRepository<PdfDocument, UUID> {

	List<PdfDocument> findAllByOrderByCreatedAtDesc();
}
