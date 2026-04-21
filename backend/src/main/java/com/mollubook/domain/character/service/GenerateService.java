package com.mollubook.domain.character.service;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.infra.claude.ClaudeClient;
import com.mollubook.infra.claude.ClaudePromptBuilder;
import com.mollubook.infra.claude.ClaudeResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class GenerateService {

	private final CharacterService characterService;
	private final ClaudePromptBuilder claudePromptBuilder;
	private final ClaudeClient claudeClient;
	private final PostRepository postRepository;

	public GenerateService(CharacterService characterService, ClaudePromptBuilder claudePromptBuilder, ClaudeClient claudeClient, PostRepository postRepository) {
		this.characterService = characterService;
		this.claudePromptBuilder = claudePromptBuilder;
		this.claudeClient = claudeClient;
		this.postRepository = postRepository;
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
}
