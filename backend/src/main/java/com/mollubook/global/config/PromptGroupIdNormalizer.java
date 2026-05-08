package com.mollubook.global.config;

import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class PromptGroupIdNormalizer {

	private final CharacterPromptRepository characterPromptRepository;
	private final CommunityPromptRepository communityPromptRepository;

	public PromptGroupIdNormalizer(
		CharacterPromptRepository characterPromptRepository,
		CommunityPromptRepository communityPromptRepository
	) {
		this.characterPromptRepository = characterPromptRepository;
		this.communityPromptRepository = communityPromptRepository;
	}

	@PostConstruct
	@Transactional
	public void normalize() {
		characterPromptRepository.findAll().forEach(prompt -> {
			if (!prompt.getId().equals(prompt.getGroupId())) {
				prompt.updateGroupId(prompt.getId());
			}
		});
		communityPromptRepository.findAll().forEach(prompt -> {
			if (!prompt.getId().equals(prompt.getGroupId())) {
				prompt.updateGroupId(prompt.getId());
			}
		});
	}
}
