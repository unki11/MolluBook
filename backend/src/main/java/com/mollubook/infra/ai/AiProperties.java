package com.mollubook.infra.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

	private final Claude claude = new Claude();
	private final Gemini gemini = new Gemini();

	public Claude getClaude() {
		return claude;
	}

	public Gemini getGemini() {
		return gemini;
	}

	public static class Claude {

		private String model = "claude-sonnet-4-6";
		private int maxTokens = 1200;

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public int getMaxTokens() {
			return maxTokens;
		}

		public void setMaxTokens(int maxTokens) {
			this.maxTokens = maxTokens;
		}
	}

	public static class Gemini {

		private String model = "gemini-2.5-flash";

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}
	}
}
