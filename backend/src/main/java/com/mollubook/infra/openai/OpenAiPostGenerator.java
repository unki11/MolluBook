package com.mollubook.infra.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.mollubook.domain.user.entity.AiModel;
import com.mollubook.domain.user.entity.UserApiKey;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.util.EncryptionUtil;
import com.mollubook.infra.ai.AiGeneratedPost;
import com.mollubook.infra.ai.AiPostGenerator;
import com.mollubook.infra.ai.AiProperties;
import com.mollubook.infra.ai.AiResponseParser;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiPostGenerator implements AiPostGenerator {

	private final WebClient webClient;
	private final EncryptionUtil encryptionUtil;
	private final AiResponseParser responseParser;
	private final AiProperties aiProperties;

	public OpenAiPostGenerator(WebClient webClient, EncryptionUtil encryptionUtil, AiResponseParser responseParser, AiProperties aiProperties) {
		this.webClient = webClient;
		this.encryptionUtil = encryptionUtil;
		this.responseParser = responseParser;
		this.aiProperties = aiProperties;
	}

	@Override
	public boolean supports(UserApiKey apiKey) {
		return apiKey.getAiModel() == AiModel.CHATGPT;
	}

	@Override
	public AiGeneratedPost generateCommunityPost(UserApiKey apiKey, String prompt) {
		return responseParser.parseTitleAndContent(generateText(apiKey, prompt, postSchema()));
	}

	@Override
	public String generateCommunityComment(UserApiKey apiKey, String prompt) {
		return responseParser.parseCommentContent(generateText(apiKey, prompt, commentSchema()));
	}

	private String generateText(UserApiKey apiKey, String prompt, Map<String, Object> schema) {
		JsonNode body = webClient.post()
			.uri("https://api.openai.com/v1/responses")
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + encryptionUtil.decrypt(apiKey.getEncryptedKey()))
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.bodyValue(Map.of(
				"model", aiProperties.getOpenai().getModel(),
				"input", prompt,
				"temperature", aiProperties.getOpenai().getTemperature(),
				"max_output_tokens", aiProperties.getOpenai().getMaxTokens(),
				"text", Map.of("format", schema)
			))
			.retrieve()
			.bodyToMono(JsonNode.class)
			.block();

		String text = extractText(body);
		if (text == null || text.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
		return text;
	}

	private String extractText(JsonNode body) {
		if (body == null || body.isMissingNode() || body.isNull()) {
			return null;
		}
		String outputText = body.path("output_text").asText(null);
		if (outputText != null && !outputText.isBlank()) {
			return outputText;
		}
		for (JsonNode outputItem : body.path("output")) {
			for (JsonNode contentItem : outputItem.path("content")) {
				String text = contentItem.path("text").asText(null);
				if (text != null && !text.isBlank()) {
					return text;
				}
			}
		}
		return null;
	}

	private Map<String, Object> postSchema() {
		return Map.of(
			"type", "json_schema",
			"name", "community_post",
			"strict", true,
			"schema", Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of("title", "content"),
				"properties", Map.of(
					"title", Map.of("type", "string"),
					"content", Map.of("type", "string")
				)
			)
		);
	}

	private Map<String, Object> commentSchema() {
		return Map.of(
			"type", "json_schema",
			"name", "community_comment",
			"strict", true,
			"schema", Map.of(
				"type", "object",
				"additionalProperties", false,
				"required", List.of("content"),
				"properties", Map.of(
					"content", Map.of("type", "string")
				)
			)
		);
	}
}
