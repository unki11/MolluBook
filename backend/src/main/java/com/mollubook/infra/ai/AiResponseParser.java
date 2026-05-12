package com.mollubook.infra.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AiResponseParser {

	private final ObjectMapper objectMapper;

	public AiResponseParser(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public AiGeneratedPost parseTitleAndContent(String rawText) {
		if (rawText == null || rawText.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}

		String normalized = stripCodeFence(rawText);
		AiGeneratedPost jsonPost = tryParseJsonPost(normalized);
		if (jsonPost != null) {
			return jsonPost;
		}

		String[] titleSplit = normalized.split("\\R", 2);
		String firstLine = titleSplit[0].trim();
		String rest = titleSplit.length > 1 ? titleSplit[1].trim() : "";

		String title = removeLabel(firstLine, "제목:", "Title:", "title:", "?쒕ぉ:");
		String content = removeLabel(rest, "내용:", "Content:", "content:", "?댁슜:");
		if (title.isBlank() || content.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
		return new AiGeneratedPost(title, content);
	}

	public String parseCommentContent(String rawText) {
		if (rawText == null || rawText.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}

		String content = stripCodeFence(rawText);
		content = tryParseJsonComment(content);
		content = removeLabel(content, "댓글:", "Comment:", "comment:", "?볤?:");
		if (content.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
		return content;
	}

	private AiGeneratedPost tryParseJsonPost(String text) {
		if (!text.startsWith("{")) {
			return null;
		}
		try {
			JsonNode root = objectMapper.readTree(text);
			String title = root.path("title").asText("").trim();
			String content = root.path("content").asText("").trim();
			return title.isBlank() || content.isBlank() ? null : new AiGeneratedPost(title, content);
		} catch (Exception ignored) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
	}

	private String tryParseJsonComment(String text) {
		if (!text.startsWith("{")) {
			return text;
		}
		try {
			JsonNode root = objectMapper.readTree(text);
			String content = root.path("content").asText("").trim();
			return content.isBlank() ? text : content;
		} catch (Exception ignored) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
	}

	private String stripCodeFence(String text) {
		String stripped = text.trim();
		if (stripped.startsWith("```")) {
			stripped = stripped.replaceFirst("^```[a-zA-Z]*\\R?", "");
			stripped = stripped.replaceFirst("\\R?```$", "");
		}
		return stripped.trim();
	}

	private String removeLabel(String text, String... labels) {
		String stripped = text.trim();
		for (String label : labels) {
			if (stripped.startsWith(label)) {
				return stripped.substring(label.length()).trim();
			}
		}
		return stripped;
	}
}
