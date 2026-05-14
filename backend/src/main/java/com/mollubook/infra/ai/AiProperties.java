package com.mollubook.infra.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {

	private final Claude claude = new Claude();
	private final Gemini gemini = new Gemini();
	private final OpenAi openai = new OpenAi();

	public Claude getClaude() {
		return claude;
	}

	public Gemini getGemini() {
		return gemini;
	}

	public OpenAi getOpenai() {
		return openai;
	}

	public static class Claude {

		private String model = "claude-haiku-4-5";
		private int maxTokens = 2048;

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
		private int maxTokens = 4096;
		private double temperature = 0.8;
		private int thinkingBudget = 0;

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

		public double getTemperature() {
			return temperature;
		}

		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}

		public int getThinkingBudget() {
			return thinkingBudget;
		}

		public void setThinkingBudget(int thinkingBudget) {
			this.thinkingBudget = thinkingBudget;
		}
	}

	public static class OpenAi {

		private String model = "gpt-4.1-mini";
		private int maxTokens = 1200;
		private double temperature = 0.8;

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

		public double getTemperature() {
			return temperature;
		}

		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}
	}
}
