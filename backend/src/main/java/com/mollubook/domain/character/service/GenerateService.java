package com.mollubook.domain.character.service;

import com.mollubook.domain.character.dto.CharacterDtos.GenerateContextResponse;
import com.mollubook.domain.character.dto.CharacterDtos.PromptSectionResponse;
import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.community.repository.CommunityRepository;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.UserApiKey;
import com.mollubook.domain.world.repository.WorldPromptRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import com.mollubook.infra.ai.AiGeneratedPost;
import com.mollubook.infra.ai.AiProviderRouter;
import com.mollubook.infra.ai.ManualCommunityPostPromptBuilder;
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
	private final ManualCommunityPostPromptBuilder manualCommunityPostPromptBuilder;
	private final AiProviderRouter aiProviderRouter;

	public GenerateService(
		CharacterService characterService,
		ClaudePromptBuilder claudePromptBuilder,
		ClaudeClient claudeClient,
		PostRepository postRepository,
		CommunityRepository communityRepository,
		WorldPromptRepository worldPromptRepository,
		CommunityPromptRepository communityPromptRepository,
		CharacterPromptRepository characterPromptRepository,
		ManualCommunityPostPromptBuilder manualCommunityPostPromptBuilder,
		AiProviderRouter aiProviderRouter
	) {
		this.characterService = characterService;
		this.claudePromptBuilder = claudePromptBuilder;
		this.claudeClient = claudeClient;
		this.postRepository = postRepository;
		this.communityRepository = communityRepository;
		this.worldPromptRepository = worldPromptRepository;
		this.communityPromptRepository = communityPromptRepository;
		this.characterPromptRepository = characterPromptRepository;
		this.manualCommunityPostPromptBuilder = manualCommunityPostPromptBuilder;
		this.aiProviderRouter = aiProviderRouter;
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
					.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
					.toList()
			));
		}
		sections.add(new PromptSectionResponse(
			"community",
			"communities_prompts",
			communityPromptRepository.findByCommunityIdOrderBySortOrderAsc(community.getId()).stream()
				.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
				.toList()
		));
		sections.add(new PromptSectionResponse(
			"character",
			"character_prompts",
			characterPromptRepository.findByCharacterIdOrderBySortOrderAsc(character.getId()).stream()
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

		UserApiKey apiKey = character.getApiKey();
		if (apiKey == null) {
			throw new CustomException(ErrorCode.COMMON_002);
		}

		String prompt = manualCommunityPostPromptBuilder.build(character, topic);
		AiGeneratedPost generatedPost = aiProviderRouter.generateCommunityPost(apiKey, prompt);
		Post post = postRepository.save(Post.builder()
			.community(character.getCommunity())
			.character(character)
			.title(generatedPost.title())
			.content(generatedPost.content())
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
