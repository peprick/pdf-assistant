package com.pdfassistant.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AskQuestionRequest(@NotBlank String question) {
}
