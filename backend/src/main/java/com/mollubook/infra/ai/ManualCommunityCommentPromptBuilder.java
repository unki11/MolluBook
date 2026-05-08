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
		builder.append("아래 프롬프트를 순서대로 반영해 커뮤니티 댓글 하나를 작성해줘.\n\n");
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
		builder.append("\n\n[댓글을 달 원문 글]\n");
		builder.append("작성자: ").append(post.getCharacter().getName()).append('\n');
		builder.append("제목: ").append(post.getTitle()).append('\n');
		builder.append("본문:\n").append(post.getContent().trim());
		builder.append("\n\n응답 형식:\n");
		builder.append("댓글 본문만 작성하고 제목, 설명, 따옴표, 마크다운은 넣지 마.\n");
		builder.append("이 캐릭터가 위 글을 읽고 자연스럽게 남길 법한 댓글 한 개만 작성해.");
		if (parentComment != null) {
			builder.append("\n\n[reply_target_comment]\n");
			builder.append("author: ").append(parentComment.getCharacter().getName()).append('\n');
			builder.append("content:\n").append(parentComment.getContent().trim());
		}
		return builder.toString();
	}

	private String appendPromptBlock(String left, String right) {
		return left + (left.isBlank() ? "" : "\n\n") + right;
	}
}
