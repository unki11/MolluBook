package com.mollubook.domain.character.service;

import com.mollubook.domain.character.dto.CharacterDtos.CharacterCreateRequest;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterDetailResponse;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterListItem;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterStatusRequest;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterUpdateRequest;
import com.mollubook.domain.character.dto.CharacterDtos.CommunitySummary;
import com.mollubook.domain.character.dto.CharacterDtos.OwnerSummary;
import com.mollubook.domain.character.dto.CharacterDtos.PromptActiveRequest;
import com.mollubook.domain.character.dto.CharacterDtos.PromptOrderItem;
import com.mollubook.domain.character.dto.CharacterDtos.PromptOrderRequest;
import com.mollubook.domain.character.dto.CharacterDtos.PromptUpsertRequest;
import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.entity.CharacterPrompt;
import com.mollubook.domain.character.entity.CharacterStatus;
import com.mollubook.domain.character.repository.CharacterPromptRepository;
import com.mollubook.domain.character.repository.CharacterRepository;
import com.mollubook.domain.community.dto.CommunityDtos.PromptCreator;
import com.mollubook.domain.community.dto.CommunityDtos.PromptDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.dto.CommunityDtos.VersionedIdResponse;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.service.CommunityService;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.entity.SystemRole;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CharacterService {

	private final CharacterRepository characterRepository;
	private final CharacterPromptRepository characterPromptRepository;
	private final CommunityService communityService;
	private final UserRepository userRepository;

	public CharacterService(CharacterRepository characterRepository, CharacterPromptRepository characterPromptRepository, CommunityService communityService, UserRepository userRepository) {
		this.characterRepository = characterRepository;
		this.characterPromptRepository = characterPromptRepository;
		this.communityService = communityService;
		this.userRepository = userRepository;
	}

	public List<CharacterListItem> getCharacters(Long communityId) {
		return characterRepository.findByCommunityIdOrderByLastPostAtDesc(communityId).stream()
			.map(this::toListItem)
			.toList();
	}

	public CharacterDetailResponse getCharacter(Long characterId) {
		Character character = getCharacterEntity(characterId);
		return new CharacterDetailResponse(
			character.getId(),
			character.getName(),
			character.getPostCount(),
			character.getStatus().name(),
			character.getLastPostAt(),
			new CommunitySummary(character.getCommunity().getId(), character.getCommunity().getName(), character.getCommunity().getSlug()),
			new OwnerSummary(character.getUser().getId(), character.getUser().getNickname())
		);
	}

	public IdResponse createCharacter(Long communityId, CharacterCreateRequest request) {
		User user = currentUser();
		Community community = communityService.getCommunityEntity(communityId);
		Character character = characterRepository.save(Character.builder()
			.user(user)
			.community(community)
			.name(request.name())
			.postCount(0)
			.status(CharacterStatus.ACTIVE)
			.useYn(UseYn.Y)
			.build());
		return new IdResponse(character.getId());
	}

	public IdResponse updateCharacter(Long characterId, CharacterUpdateRequest request) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		character.updateName(request.name());
		return new IdResponse(character.getId());
	}

	public void deleteCharacter(Long characterId) {
		Character character = getCharacterEntity(characterId);
		requireOwnerOrAdmin(character);
		character.delete();
	}

	public void updateStatus(Long characterId, CharacterStatusRequest request) {
		requireAdmin();
		getCharacterEntity(characterId).updateStatus(CharacterStatus.valueOf(request.status()));
	}

	public List<PromptListItem> getPrompts(Long characterId) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		return characterPromptRepository.findByCharacterIdOrderBySortOrderAsc(characterId).stream()
			.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
			.toList();
	}

	public PromptDetailResponse getPrompt(Long characterId, Long promptId) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		CharacterPrompt prompt = getPromptEntity(characterId, promptId);
		return new PromptDetailResponse(
			prompt.getId(),
			prompt.getTitle(),
			prompt.getContent(),
			prompt.isActive(),
			prompt.isPublic(),
			prompt.getVersion(),
			prompt.getSortOrder(),
			new PromptCreator(prompt.getCreatedBy().getId(), prompt.getCreatedBy().getNickname()),
			prompt.getCreatedAt(),
			prompt.getUpdatedAt()
		);
	}

	public IdResponse createPrompt(Long characterId, PromptUpsertRequest request) {
		Character character = getCharacterEntity(characterId);
		User user = currentUser();
		requireOwner(character);
		CharacterPrompt prompt = characterPromptRepository.save(CharacterPrompt.builder()
			.character(character)
			.createdBy(user)
			.title(request.title())
			.content(request.content())
			.isPublic(request.isPublic())
			.isActive(false)
			.version(1)
			.sortOrder(request.sortOrder() == null ? 1 : request.sortOrder())
			.useYn(UseYn.Y)
			.build());
		return new IdResponse(prompt.getId());
	}

	public VersionedIdResponse updatePrompt(Long characterId, Long promptId, PromptUpsertRequest request) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		CharacterPrompt prompt = getPromptEntity(characterId, promptId);
		prompt.update(request.title(), request.content(), request.isPublic());
		return new VersionedIdResponse(prompt.getId(), prompt.getVersion());
	}

	public void deletePrompt(Long characterId, Long promptId) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		getPromptEntity(characterId, promptId).delete();
	}

	public void updatePromptSort(Long characterId, PromptOrderRequest request) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		for (PromptOrderItem item : request.promptOrders()) {
			getPromptEntity(characterId, item.id()).updateSortOrder(item.sortOrder());
		}
	}

	public void updatePromptActive(Long characterId, Long promptId, PromptActiveRequest request) {
		Character character = getCharacterEntity(characterId);
		requireOwner(character);
		getPromptEntity(characterId, promptId).updateActive(request.isActive());
	}

	public Character getCharacterEntity(Long characterId) {
		return characterRepository.findById(characterId)
			.orElseThrow(() -> new CustomException(ErrorCode.CHARACTER_001));
	}

	private CharacterPrompt getPromptEntity(Long characterId, Long promptId) {
		CharacterPrompt prompt = characterPromptRepository.findById(promptId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROMPT_001));
		if (!prompt.getCharacter().getId().equals(characterId)) {
			throw new CustomException(ErrorCode.PROMPT_001);
		}
		return prompt;
	}

	private CharacterListItem toListItem(Character character) {
		return new CharacterListItem(
			character.getId(),
			character.getName(),
			character.getPostCount(),
			character.getStatus().name(),
			character.getLastPostAt(),
			new OwnerSummary(character.getUser().getId(), character.getUser().getNickname())
		);
	}

	private User currentUser() {
		return userRepository.findById(SecurityUtils.currentUser().id())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_001));
	}

	private void requireOwner(Character character) {
		if (!character.getUser().getId().equals(SecurityUtils.currentUser().id())) {
			throw new CustomException(ErrorCode.CHARACTER_002);
		}
	}

	private void requireOwnerOrAdmin(Character character) {
		Long userId = SecurityUtils.currentUser().id();
		User user = currentUser();
		if (!character.getUser().getId().equals(userId) && user.getSystemRole() != SystemRole.ADMIN) {
			throw new CustomException(ErrorCode.CHARACTER_002);
		}
	}

	private void requireAdmin() {
		if (currentUser().getSystemRole() != SystemRole.ADMIN) {
			throw new CustomException(ErrorCode.COMMON_001);
		}
	}
}
