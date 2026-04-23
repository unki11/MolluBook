package com.mollubook.domain.community.service;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.character.repository.CharacterRepository;
import com.mollubook.domain.community.dto.CommunityDtos.CharacterSummary;
import com.mollubook.domain.community.dto.CommunityDtos.CommunityDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.CommunityListItem;
import com.mollubook.domain.community.dto.CommunityDtos.CreateCommunityRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptActiveRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptCreator;
import com.mollubook.domain.community.dto.CommunityDtos.PromptDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptUpsertRequest;
import com.mollubook.domain.community.dto.CommunityDtos.UpdateCommunityRequest;
import com.mollubook.domain.community.dto.CommunityDtos.VersionedIdResponse;
import com.mollubook.domain.community.dto.CommunityDtos.WorldSummary;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.community.entity.CommunityManager;
import com.mollubook.domain.community.entity.CommunityPrompt;
import com.mollubook.domain.community.entity.ManagerRole;
import com.mollubook.domain.community.repository.CommunityManagerRepository;
import com.mollubook.domain.community.repository.CommunityPromptRepository;
import com.mollubook.domain.community.repository.CommunityRepository;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.entity.SystemRole;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.domain.world.entity.World;
import com.mollubook.domain.world.repository.WorldRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CommunityService {

	private final CommunityRepository communityRepository;
	private final CommunityManagerRepository communityManagerRepository;
	private final CommunityPromptRepository communityPromptRepository;
	private final CharacterRepository characterRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final WorldRepository worldRepository;

	public CommunityService(CommunityRepository communityRepository, CommunityManagerRepository communityManagerRepository, CommunityPromptRepository communityPromptRepository, CharacterRepository characterRepository, PostRepository postRepository, UserRepository userRepository, WorldRepository worldRepository) {
		this.communityRepository = communityRepository;
		this.communityManagerRepository = communityManagerRepository;
		this.communityPromptRepository = communityPromptRepository;
		this.characterRepository = characterRepository;
		this.postRepository = postRepository;
		this.userRepository = userRepository;
		this.worldRepository = worldRepository;
	}

	public List<CommunityListItem> getCommunities() {
		return communityRepository.findAll().stream()
			.map(this::toListItem)
			.toList();
	}

	public List<CommunityListItem> getCommunities(Long worldId) {
		return communityRepository.findByWorldIdOrderByNameAsc(worldId).stream()
			.map(this::toListItem)
			.toList();
	}

	public CommunityDetailResponse getCommunity(String slug) {
		Community community = communityRepository.findBySlug(slug)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_001));
		List<CharacterSummary> characters = characterRepository.findByCommunityIdOrderByLastPostAtDesc(community.getId()).stream()
			.map(character -> new CharacterSummary(character.getId(), character.getName(), character.getPostCount(), character.getStatus().name(), character.getLastPostAt()))
			.toList();
		return new CommunityDetailResponse(community.getId(), community.getName(), community.getSlug(), community.getDescription(), community.getThumbnailUrl(), toWorldSummary(community.getWorld()), characters);
	}

	public IdResponse createCommunity(CreateCommunityRequest request) {
		User user = currentUser();
		requireAdmin(user);
		World world = request.worldId() == null ? null : getWorldEntity(request.worldId());
		Community community = communityRepository.save(Community.builder()
			.world(world)
			.name(request.name())
			.slug(request.slug())
			.description(request.description())
			.thumbnailUrl(request.thumbnailUrl())
			.isPrivate(false)
			.build());
		communityManagerRepository.save(CommunityManager.builder()
			.user(user)
			.community(community)
			.managerRole(ManagerRole.OWNER)
			.build());
		return new IdResponse(community.getId());
	}

	public IdResponse updateCommunity(Long communityId, UpdateCommunityRequest request) {
		requireAdmin(currentUser());
		Community community = getCommunityEntity(communityId);
		community.update(request.name(), request.description(), request.thumbnailUrl());
		community.updateWorld(request.worldId() == null ? null : getWorldEntity(request.worldId()));
		return new IdResponse(community.getId());
	}

	public void deleteCommunity(Long communityId) {
		requireAdmin(currentUser());
		communityRepository.delete(getCommunityEntity(communityId));
	}

	public List<PromptListItem> getPrompts(Long communityId) {
		getCommunityEntity(communityId);
		requireCommunityManager(communityId);
		return communityPromptRepository.findByCommunityIdOrderBySortOrderAsc(communityId).stream()
			.map(prompt -> new PromptListItem(prompt.getId(), prompt.getTitle(), prompt.getContent(), prompt.isActive(), prompt.isPublic(), prompt.getVersion(), prompt.getSortOrder(), prompt.getCreatedAt()))
			.toList();
	}

	public PromptDetailResponse getPrompt(Long communityId, Long promptId) {
		requireCommunityManager(communityId);
		CommunityPrompt prompt = getPromptEntity(communityId, promptId);
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

	public IdResponse createPrompt(Long communityId, PromptUpsertRequest request) {
		User user = currentUser();
		requireCommunityManager(communityId);
		Community community = getCommunityEntity(communityId);
		CommunityPrompt prompt = communityPromptRepository.save(CommunityPrompt.builder()
			.community(community)
			.createdBy(user)
			.title(request.title())
			.content(request.content())
			.isPublic(request.isPublic())
			.isActive(false)
			.version(1)
			.sortOrder(request.sortOrder() == null ? 1 : request.sortOrder())
			.groupId(newPromptGroupId())
			.useYn(UseYn.Y)
			.build());
		return new IdResponse(prompt.getId());
	}

	public VersionedIdResponse updatePrompt(Long communityId, Long promptId, PromptUpsertRequest request) {
		requireCommunityManager(communityId);
		CommunityPrompt prompt = getPromptEntity(communityId, promptId);
		prompt.update(request.title(), request.content(), request.isPublic());
		return new VersionedIdResponse(prompt.getId(), prompt.getVersion());
	}

	public void deletePrompt(Long communityId, Long promptId) {
		requireCommunityManager(communityId);
		getPromptEntity(communityId, promptId).delete();
	}

	public void updatePromptSort(Long communityId, PromptOrderRequest request) {
		requireCommunityManager(communityId);
		for (PromptOrderItem item : request.promptOrders()) {
			getPromptEntity(communityId, item.id()).updateSortOrder(item.sortOrder());
		}
	}

	public void updatePromptActive(Long communityId, Long promptId, PromptActiveRequest request) {
		requireCommunityManager(communityId);
		getPromptEntity(communityId, promptId).updateActive(request.isActive());
	}

	public Community getCommunityEntity(Long communityId) {
		return communityRepository.findById(communityId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_001));
	}

	public void requireCommunityManager(Long communityId) {
		User user = currentUser();
		if (user.getSystemRole() == SystemRole.ADMIN) {
			return;
		}
		communityManagerRepository.findByUserIdAndCommunityId(user.getId(), communityId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMUNITY_002));
	}

	private CommunityPrompt getPromptEntity(Long communityId, Long promptId) {
		CommunityPrompt prompt = communityPromptRepository.findById(promptId)
			.orElseThrow(() -> new CustomException(ErrorCode.PROMPT_001));
		if (!prompt.getCommunity().getId().equals(communityId)) {
			throw new CustomException(ErrorCode.PROMPT_001);
		}
		return prompt;
	}

	private User currentUser() {
		return userRepository.findById(SecurityUtils.currentUser().id())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_001));
	}

	private void requireAdmin(User user) {
		if (user.getSystemRole() != SystemRole.ADMIN) {
			throw new CustomException(ErrorCode.COMMON_001);
		}
	}

	private World getWorldEntity(Long worldId) {
		return worldRepository.findById(worldId)
			.orElseThrow(() -> new CustomException(ErrorCode.WORLD_001));
	}

	private CommunityListItem toListItem(Community community) {
		return new CommunityListItem(
			community.getId(),
			community.getName(),
			community.getSlug(),
			community.getDescription(),
			community.getThumbnailUrl(),
			toWorldSummary(community.getWorld()),
			characterRepository.findByCommunityIdOrderByLastPostAtDesc(community.getId()).size(),
			postRepository.countByCommunityId(community.getId())
		);
	}

	private WorldSummary toWorldSummary(World world) {
		return world == null ? null : new WorldSummary(world.getId(), world.getName(), world.getSlug());
	}

	private long newPromptGroupId() {
		return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
	}
}
