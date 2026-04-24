package com.mollubook.domain.character.service;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.community.repository.CommunityRepository;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.world.repository.WorldPromptRepository;
import com.mollubook.domain.character.dto.CharacterDtos.GenerateContextResponse;
import com.mollubook.domain.character.dto.CharacterDtos.PromptSectionResponse;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import com.mollubook.infra.claude.ClaudeClient;
import com.mollubook.infra.claude.ClaudePromptBuilder;
import com.mollubook.infra.claude.ClaudeResponse;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GenerateService {

	private final CharacterService characterService;
	private final ClaudePromptBuilder claudePromptBuilder;
	private final ClaudeClient claudeClient;
	private final PostRepository postRepository;
	private final CommunityRepository communityRepository;
	private final WorldPromptRepository worldPromptRepository;
	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterPromptRepository characterPromptRepository;

	public GenerateService(CharacterService characterService, ClaudePromptBuilder claudePromptBuilder, ClaudeClient claudeClient, PostRepository postRepository, CommunityRepository communityRepository, WorldPromptRepository worldPromptRepository, CommunityPromptRepository communityPromptRepository, CharacterPromptRepository characterPromptRepository) {
		this.characterService = characterService;
		this.claudePromptBuilder = claudePromptBuilder;
		this.claudeClient = claudeClient;
		this.postRepository = postRepository;
		this.communityRepository = communityRepository;
		this.worldPromptRepository = worldPromptRepository;
		this.communityPromptRepository = communityPromptRepository;
		this.characterPromptRepository = characterPromptRepository;
	}

	public GenerateContextResponse getGenerateContext(Long characterId) {
		Character character = characterService.getCharacterEntity(characterId);
		requireOwner(character);
		Community community = communityRepository.findById(character.getCommunity().getId())
			.orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_001));

		List<PromptSectionResponse> sections = new ArrayList<>();
		if (community.getWorld() != null) {
			sections.add(new PromptSectionResponse(
				"world",
				"worlds_prompts",
				worldPromptRepository.findByWorldIdOrderBySortOrderAsc(community.getWorld().getId()).stream()
					.filter(prompt -> prompt.isActive())
					.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
					.toList()
			));
		}
		sections.add(new PromptSectionResponse(
			"community",
			"communities_prompts",
			communityPromptRepository.findByCommunityIdOrderBySortOrderAsc(community.getId()).stream()
				.filter(prompt -> prompt.isActive())
				.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
				.toList()
		));
		sections.add(new PromptSectionResponse(
			"character",
			"character_prompts",
			characterPromptRepository.findByCharacterIdOrderBySortOrderAsc(character.getId()).stream()
				.filter(prompt -> prompt.isActive())
				.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
				.toList()
		));
		return new GenerateContextResponse(character.getId(), character.getName(), sections);
	}

	public IdResponse generate(Long characterId, String topic) {
		Character character = characterService.getCharacterEntity(characterId);
		if (character.getStatus() != com.mollubook.domain.character.entity.CharacterStatus.ACTIVE) {
			throw new CustomException(ErrorCode.CHARACTER_003);
		}
		String prompt = claudePromptBuilder.build(character, topic);
		ClaudeResponse response = claudeClient.generatePost(prompt);
		Post post = postRepository.save(Post.builder()
			.community(character.getCommunity())
			.character(character)
			.title(response.title())
			.content(response.content())
			.likeCount(0)
			.dislikeCount(0)
			.commentCount(0)
			.useYn(UseYn.Y)
			.build());
		character.incrementPostCount();
		return new IdResponse(post.getId());
	}

	public IdResponse generateManual(Long characterId, String topic) {
		Character character = characterService.getCharacterEntity(characterId);
		requireOwner(character);
		if (character.getStatus() != com.mollubook.domain.character.entity.CharacterStatus.ACTIVE) {
			throw new CustomException(ErrorCode.CHARACTER_003);
		}

		String safeTopic = topic == null || topic.isBlank() ? "자유 주제" : topic.trim();
		Post post = postRepository.save(Post.builder()
			.community(character.getCommunity())
			.character(character)
			.title("[임시] " + character.getName() + "의 수동 생성 글")
			.content("이 글은 AI 연동 전 수동 생성 테스트용 임시 글입니다.\n\n주제: " + safeTopic + "\n\n실제 생성 시 world/community/character 활성 프롬프트가 함께 사용됩니다.")
			.likeCount(0)
			.dislikeCount(0)
			.commentCount(0)
			.useYn(UseYn.Y)
			.build());
		character.incrementPostCount();
		return new IdResponse(post.getId());
	}

	private void requireOwner(Character character) {
		if (!character.getUser().getId().equals(SecurityUtils.currentUser().id())) {
			throw new CustomException(ErrorCode.CHARACTER_002);
		}
	}
}
