package com.pdfassistant.backend.dto;

import java.util.List;

public record AskQuestionResponse(String answer, List<SourceResponse> sources) {
}
