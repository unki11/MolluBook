package com.mollubook.infra.claude;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.world.repository.WorldPromptRepository;
import org.springframework.stereotype.Component;

@Component
public class ClaudePromptBuilder {

	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterPromptRepository characterPromptRepository;
	private final WorldPromptRepository worldPromptRepository;

	public ClaudePromptBuilder(CommunityPromptRepository communityPromptRepository, CharacterPromptRepository characterPromptRepository, WorldPromptRepository worldPromptRepository) {
		this.communityPromptRepository = communityPromptRepository;
		this.characterPromptRepository = characterPromptRepository;
		this.worldPromptRepository = worldPromptRepository;
	}

	public String build(Character character, String topic) {
		String worldPrompt = character.getCommunity().getWorld() == null
			? ""
			: worldPromptRepository.findByWorldIdOrderBySortOrderAsc(character.getCommunity().getWorld().getId()).stream()
				.filter(prompt -> prompt.isActive())
				.map(prompt -> prompt.getContent())
				.reduce("", (left, right) -> left + "\n" + right);
		String communityPrompt = communityPromptRepository.findByCommunityIdOrderBySortOrderAsc(character.getCommunity().getId()).stream()
			.filter(prompt -> prompt.isActive())
			.map(prompt -> prompt.getContent())
			.reduce("", (left, right) -> left + "\n" + right);
		String characterPrompt = characterPromptRepository.findByCharacterIdOrderBySortOrderAsc(character.getId()).stream()
			.filter(prompt -> prompt.isActive())
			.map(prompt -> prompt.getContent())
			.reduce("", (left, right) -> left + "\n" + right);
		return "World Prompt:\n" + worldPrompt + "\n\nCommunity Prompt:\n" + communityPrompt + "\n\nCharacter Prompt:\n" + characterPrompt + "\n\nTopic:\n" + (topic == null ? "자유 주제" : topic);
	}
}
