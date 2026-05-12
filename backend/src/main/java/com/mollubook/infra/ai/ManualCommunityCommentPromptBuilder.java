package com.mollubook.infra.ai;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.comment.entity.Comment;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.world.repository.WorldPromptRepository;
import org.springframework.stereotype.Component;

@Component
public class ManualCommunityCommentPromptBuilder {

	private final WorldPromptRepository worldPromptRepository;
	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterPromptRepository characterPromptRepository;

	public ManualCommunityCommentPromptBuilder(
		WorldPromptRepository worldPromptRepository,
		CommunityPromptRepository communityPromptRepository,
		CharacterPromptRepository characterPromptRepository
	) {
		this.worldPromptRepository = worldPromptRepository;
		this.communityPromptRepository = communityPromptRepository;
		this.characterPromptRepository = characterPromptRepository;
	}

	public String build(Character character, Post post, String topic) {
		return build(character, post, null, topic);
	}

	public String build(Character character, Post post, Comment parentComment, String topic) {
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
		builder.append("아래 프롬프트를 순서대로 반영해서 캐릭터가 커뮤니티 댓글 하나를 작성하게 해.\n\n");
		builder.append("[world_prompts]\n");
		builder.append(worldPrompt.isBlank() ? "(없음)" : worldPrompt);
		builder.append("\n\n[community_prompts]\n");
		builder.append(communityPrompt.isBlank() ? "(없음)" : communityPrompt);
		builder.append("\n\n[character_prompts]\n");
		builder.append(characterPrompt.isBlank() ? "(없음)" : characterPrompt);
		if (topic != null && !topic.isBlank()) {
			builder.append("\n\n[추가 주제]\n");
			builder.append(topic.trim());
		}
		builder.append("\n\n[원문 글]\n");
		builder.append("작성자: ").append(post.getCharacter().getName()).append('\n');
		builder.append("제목: ").append(post.getTitle()).append('\n');
		builder.append("본문:\n").append(post.getContent().trim());
		if (parentComment != null) {
			builder.append("\n\n[답글 대상 댓글]\n");
			builder.append("작성자: ").append(parentComment.getCharacter().getName()).append('\n');
			builder.append("본문:\n").append(parentComment.getContent().trim());
		}
		builder.append("\n\n응답은 JSON 객체 하나만 반환해. 마크다운 코드블록은 쓰지 마.\n");
		builder.append("{\"content\":\"댓글 본문\"}\n");
		builder.append("제목, 설명, 따옴표 없이 이 캐릭터가 자연스럽게 남길 법한 댓글 한 개만 작성해.");
		return builder.toString();
	}

	private String appendPromptBlock(String left, String right) {
		return left + (left.isBlank() ? "" : "\n\n") + right;
	}
}
