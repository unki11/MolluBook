package com.mollubook.infra.claude;

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
public class ClaudeAiPostGenerator implements AiPostGenerator {

	private final WebClient webClient;
	private final EncryptionUtil encryptionUtil;
	private final AiResponseParser responseParser;
	private final AiProperties aiProperties;

	public ClaudeAiPostGenerator(WebClient webClient, EncryptionUtil encryptionUtil, AiResponseParser responseParser, AiProperties aiProperties) {
		this.webClient = webClient;
		this.encryptionUtil = encryptionUtil;
		this.responseParser = responseParser;
		this.aiProperties = aiProperties;
	}

	@Override
	public boolean supports(UserApiKey apiKey) {
		return apiKey.getAiModel() == AiModel.CLAUDE;
	}

	@Override
	public AiGeneratedPost generateCommunityPost(UserApiKey apiKey, String prompt) {
		JsonNode body = webClient.post()
			.uri("https://api.anthropic.com/v1/messages")
			.header("x-api-key", encryptionUtil.decrypt(apiKey.getEncryptedKey()))
			.header("anthropic-version", "2023-06-01")
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.bodyValue(Map.of(
				"model", aiProperties.getClaude().getModel(),
				"max_tokens", aiProperties.getClaude().getMaxTokens(),
				"messages", List.of(Map.of("role", "user", "content", prompt))
			))
			.retrieve()
			.bodyToMono(JsonNode.class)
			.block();

		String text = body == null ? null : body.path("content").path(0).path("text").asText(null);
		if (text == null || text.isBlank()) {
			throw new CustomException(ErrorCode.COMMON_002);
		}
		return responseParser.parseTitleAndContent(text);
	}
}
