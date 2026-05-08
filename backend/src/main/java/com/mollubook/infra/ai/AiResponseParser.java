package com.mollubook.infra.ai;

import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AiResponseParser {

	public AiGeneratedPost parseTitleAndContent(String rawText) {
		if (rawText == null || rawText.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}

		String normalized = rawText.trim();
		String[] titleSplit = normalized.split("\\R", 2);
		String firstLine = titleSplit[0].trim();
		String rest = titleSplit.length > 1 ? titleSplit[1].trim() : "";

		String title = firstLine.startsWith("제목:") ? firstLine.substring(3).trim() : firstLine;
		String content = rest;
		if (content.startsWith("내용:")) {
			content = content.substring(3).trim();
		}
		if (title.isBlank() || content.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
		return new AiGeneratedPost(title, content);
	}

	public String parseCommentContent(String rawText) {
		if (rawText == null || rawText.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}

		String content = rawText.trim();
		if (content.startsWith("댓글:")) {
			content = content.substring(3).trim();
		}
		if (content.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
		return content;
	}
}
