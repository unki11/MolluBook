package com.mollubook.domain.world.service;

import com.mollubook.domain.character.repository.CharacterRepository;
import com.mollubook.domain.community.dto.CommunityDtos.PromptActiveRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptCreator;
import com.mollubook.domain.community.dto.CommunityDtos.PromptDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptUpsertRequest;
import com.mollubook.domain.community.dto.CommunityDtos.VersionedIdResponse;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.repository.CommunityRepository;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.entity.SystemRole;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.domain.world.dto.WorldDtos.CommunitySummary;
import com.mollubook.domain.world.dto.WorldDtos.WorldDetailResponse;
import com.mollubook.domain.world.dto.WorldDtos.WorldListItem;
import com.mollubook.domain.world.dto.WorldDtos.WorldUpsertRequest;
import com.mollubook.domain.world.entity.World;
import com.mollubook.domain.world.entity.WorldPrompt;
import com.mollubook.domain.world.repository.WorldPromptRepository;
import com.mollubook.domain.world.repository.WorldRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class WorldService {

	private final WorldRepository worldRepository;
	private final WorldPromptRepository worldPromptRepository;
	private final CommunityRepository communityRepository;
	private final CharacterRepository characterRepository;
	private final UserRepository userRepository;

	public WorldService(WorldRepository worldRepository, WorldPromptRepository worldPromptRepository, CommunityRepository communityRepository, CharacterRepository characterRepository, UserRepository userRepository) {
		this.worldRepository = worldRepository;
		this.worldPromptRepository = worldPromptRepository;
		this.communityRepository = communityRepository;
		this.characterRepository = characterRepository;
		this.userRepository = userRepository;
	}

	public List<WorldListItem> getWorlds() {
		return worldRepository.findAll().stream()
			.map(world -> new WorldListItem(
				world.getId(),
				world.getName(),
				world.getSlug(),
				world.getDescription(),
				world.getThumbnailUrl(),
				communityRepository.countByWorldId(world.getId())
			))
			.toList();
	}

	public WorldDetailResponse getWorld(String slug) {
		World world = worldRepository.findBySlug(slug)
			.orElseThrow(() -> new CustomException(ErrorCode.WORLD_001));
		List<CommunitySummary> communities = communityRepository.findByWorldIdOrderByNameAsc(world.getId()).stream()
			.map(community -> new CommunitySummary(
				community.getId(),
				community.getName(),
				community.getSlug(),
				characterRepository.findByCommunityIdOrderByLastPostAtDesc(community.getId()).size()
			))
			.toList();
		return new WorldDetailResponse(world.getId(), world.getName(), world.getSlug(), world.getDescription(), world.getThumbnailUrl(), communities);
	}

	public IdResponse createWorld(WorldUpsertRequest request) {
		requireAdmin();
		World world = worldRepository.save(World.builder()
			.name(request.name())
			.slug(request.slug())
			.description(request.description())
			.thumbnailUrl(request.thumbnailUrl())
			.isPrivate(false)
			.build());
		return new IdResponse(world.getId());
	}

	public IdResponse updateWorld(Long worldId, WorldUpsertRequest request) {
		requireAdmin();
		World world = getWorldEntity(worldId);
		world.update(request.name(), request.description(), request.thumbnailUrl());
		return new IdResponse(world.getId());
	}

	public void deleteWorld(Long worldId) {
		requireAdmin();
		worldRepository.delete(getWorldEntity(worldId));
	}

	public List<PromptListItem> getPrompts(Long worldId) {
		requireAdmin();
		getWorldEntity(worldId);
		return worldPromptRepository.findByWorldIdOrderBySortOrderAsc(worldId).stream()
			.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
			.toList();
	}

	public PromptDetailResponse getPrompt(Long worldId, Long promptId) {
		requireAdmin();
		WorldPrompt prompt = getPromptEntity(worldId, promptId);
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

	public IdResponse createPrompt(Long worldId, PromptUpsertRequest request) {
		requireAdmin();
		World world = getWorldEntity(worldId);
		User user = currentUser();
		WorldPrompt prompt = worldPromptRepository.save(WorldPrompt.builder()
			.world(world)
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

	public VersionedIdResponse updatePrompt(Long worldId, Long promptId, PromptUpsertRequest request) {
		requireAdmin();
		WorldPrompt prompt = getPromptEntity(worldId, promptId);
		prompt.update(request.title(), request.content(), request.isPublic());
		return new VersionedIdResponse(prompt.getId(), prompt.getVersion());
	}

	public void deletePrompt(Long worldId, Long promptId) {
		requireAdmin();
		getPromptEntity(worldId, promptId).delete();
	}

	public void updatePromptSort(Long worldId, PromptOrderRequest request) {
		requireAdmin();
		for (PromptOrderItem item : request.promptOrders()) {
			getPromptEntity(worldId, item.id()).updateSortOrder(item.sortOrder());
		}
	}

	public void updatePromptActive(Long worldId, Long promptId, PromptActiveRequest request) {
		requireAdmin();
		getPromptEntity(worldId, promptId).updateActive(request.isActive());
	}

	public World getWorldEntity(Long worldId) {
		return worldRepository.findById(worldId)
			.orElseThrow(() -> new CustomException(ErrorCode.WORLD_001));
	}

	private WorldPrompt getPromptEntity(Long worldId, Long promptId) {
		WorldPrompt prompt = worldPromptRepository.findById(promptId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROMPT_001));
		if (!prompt.getWorld().getId().equals(worldId)) {
			throw new CustomException(ErrorCode.PROMPT_001);
		}
		return prompt;
	}

	private User currentUser() {
		return userRepository.findById(SecurityUtils.currentUser().id())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_001));
	}

	private void requireAdmin() {
		if (currentUser().getSystemRole() != SystemRole.ADMIN) {
			throw new CustomException(ErrorCode.COMMON_001);
		}
	}
}
