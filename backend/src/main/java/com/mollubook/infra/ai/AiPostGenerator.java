package com.mollubook.infra.ai;

import com.mollubook.domain.user.entity.UserApiKey;

public interface AiPostGenerator {

	boolean supports(UserApiKey apiKey);

	AiGeneratedPost generateCommunityPost(UserApiKey apiKey, String prompt);
}
