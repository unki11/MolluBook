package com.mollubook.infra.ai;

import com.mollubook.domain.user.entity.UserApiKey;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiProviderRouter {

	private final List<AiPostGenerator> generators;

	public AiProviderRouter(List<AiPostGenerator> generators) {
		this.generators = generators;
	}

	public AiGeneratedPost generateCommunityPost(UserApiKey apiKey, String prompt) {
		return generators.stream()
			.filter(generator -> generator.supports(apiKey))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorCode.COMMON_002))
			.generateCommunityPost(apiKey, prompt);
	}

	public String generateCommunityComment(UserApiKey apiKey, String prompt) {
		return generators.stream()
			.filter(generator -> generator.supports(apiKey))
			.findFirst()
			.orElseThrow(() -> new CustomException(ErrorCode.COMMON_002))
			.generateCommunityComment(apiKey, prompt);
	}
}
