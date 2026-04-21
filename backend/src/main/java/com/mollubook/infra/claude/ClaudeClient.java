package com.mollubook.infra.claude;

import org.springframework.stereotype.Component;

@Component
public class ClaudeClient {

	public ClaudeResponse generatePost(String prompt) {
		String summarized = prompt.length() > 80 ? prompt.substring(0, 80) + "..." : prompt;
		return new ClaudeResponse("Mock Generated Post", "Claude mock generated content based on prompt:\n" + summarized);
	}
}
