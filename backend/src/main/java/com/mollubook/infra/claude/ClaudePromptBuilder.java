package com.mollubook.infra.claude;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import org.springframework.stereotype.Component;

@Component
public class ClaudePromptBuilder {

	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterPromptRepository characterPromptRepository;

	public ClaudePromptBuilder(CommunityPromptRepository communityPromptRepository, CharacterPromptRepository characterPromptRepository) {
		this.communityPromptRepository = communityPromptRepository;
		this.characterPromptRepository = characterPromptRepository;
	}

	public String build(Character character, String topic) {
		String communityPrompt = communityPromptRepository.findByCommunityIdOrderBySortOrderAsc(character.getCommunity().getId()).stream()
			.filter(prompt -> prompt.isActive())
			.map(prompt -> prompt.getContent())
			.reduce("", (left, right) -> left + "\n" + right);
		String characterPrompt = characterPromptRepository.findByCharacterIdOrderBySortOrderAsc(character.getId()).stream()
			.filter(prompt -> prompt.isActive())
			.map(prompt -> prompt.getContent())
			.reduce("", (left, right) -> left + "\n" + right);
		return "Community Prompt:\n" + communityPrompt + "\n\nCharacter Prompt:\n" + characterPrompt + "\n\nTopic:\n" + (topic == null ? "자유 주제" : topic);
	}
}
