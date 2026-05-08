package com.mollubook.infra.ai;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.world.repository.WorldPromptRepository;
import org.springframework.stereotype.Component;

@Component
public class ManualCommunityPostPromptBuilder {

	private static final String FINAL_INSTRUCTION = "해당 설정을 기반으로 해당 캐릭터가 작성할만한 커뮤니티 글과 제목을 작성해줘";

	private final WorldPromptRepository worldPromptRepository;
	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterPromptRepository characterPromptRepository;

	public ManualCommunityPostPromptBuilder(
		WorldPromptRepository worldPromptRepository,
		CommunityPromptRepository communityPromptRepository,
		CharacterPromptRepository characterPromptRepository
	) {
		this.worldPromptRepository = worldPromptRepository;
		this.communityPromptRepository = communityPromptRepository;
		this.characterPromptRepository = characterPromptRepository;
	}

	public String build(Character character, String topic) {
		Community community = character.getCommunity();
		String worldPrompt = community.getWorld() == null
			? ""
			: worldPromptRepository.findByWorldIdOrderBySortOrderAsc(community.getWorld().getId()).stream()
				.filter(prompt -> prompt.isActive())
				.map(prompt -> prompt.getContent())
				.reduce("", this::appendPromptBlock);
		String communityPrompt = communityPromptRepository.findByCommunityIdOrderBySortOrderAsc(community.getId()).stream()
			.filter(prompt -> prompt.isActive())
			.map(prompt -> prompt.getContent())
			.reduce("", this::appendPromptBlock);
		String characterPrompt = characterPromptRepository.findByCharacterIdOrderBySortOrderAsc(character.getId()).stream()
			.filter(prompt -> prompt.isActive())
			.map(prompt -> prompt.getContent())
			.reduce("", this::appendPromptBlock);

		StringBuilder builder = new StringBuilder();
		builder.append("아래 프롬프트를 순서대로 적용해 커뮤니티 글을 작성해줘.\n\n");
		builder.append("[worlds_prompts]\n");
		builder.append(worldPrompt.isBlank() ? "(없음)" : worldPrompt);
		builder.append("\n\n[communities_prompts]\n");
		builder.append(communityPrompt.isBlank() ? "(없음)" : communityPrompt);
		builder.append("\n\n[character_prompts]\n");
		builder.append(characterPrompt.isBlank() ? "(없음)" : characterPrompt);
		if (topic != null && !topic.isBlank()) {
			builder.append("\n\n[추가 주제]\n");
			builder.append(topic.trim());
		}
		builder.append("\n\n응답 형식:\n");
		builder.append("제목: 한 줄 제목\n");
		builder.append("내용:\n본문 여러 줄\n\n");
		builder.append(FINAL_INSTRUCTION);
		return builder.toString();
	}

	private String appendPromptBlock(String left, String right) {
		return left + (left.isBlank() ? "" : "\n\n") + right;
	}
}
