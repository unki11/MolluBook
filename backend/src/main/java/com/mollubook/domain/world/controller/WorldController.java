package com.mollubook.domain.world.controller;

import com.mollubook.domain.community.dto.CommunityDtos.CommunityListItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptActiveRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptUpsertRequest;
import com.mollubook.domain.community.service.CommunityService;
import com.mollubook.domain.post.dto.PostDtos.PostListResponse;
import com.mollubook.domain.post.service.PostService;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.world.dto.WorldDtos.WorldDetailResponse;
import com.mollubook.domain.world.dto.WorldDtos.WorldListItem;
import com.mollubook.domain.world.dto.WorldDtos.WorldUpsertRequest;
import com.mollubook.domain.world.service.WorldService;
import com.mollubook.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorldController {

	private final WorldService worldService;
	private final CommunityService communityService;
	private final PostService postService;

	public WorldController(WorldService worldService, CommunityService communityService, PostService postService) {
		this.worldService = worldService;
		this.communityService = communityService;
		this.postService = postService;
	}

	@GetMapping("/api/worlds")
	public ApiResponse<List<WorldListItem>> getWorlds() {
		return ApiResponse.ok(worldService.getWorlds());
	}

	@GetMapping("/api/worlds/{slug}")
	public ApiResponse<WorldDetailResponse> getWorld(@PathVariable String slug) {
		return ApiResponse.ok(worldService.getWorld(slug));
	}

	@GetMapping("/api/worlds/{worldId}/communities")
	public ApiResponse<List<CommunityListItem>> getWorldCommunities(@PathVariable Long worldId) {
		return ApiResponse.ok(communityService.getCommunities(worldId));
	}

	@GetMapping("/api/worlds/{worldId}/posts")
	public ApiResponse<PostListResponse> getWorldPosts(
		@PathVariable Long worldId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) Long characterId
	) {
		return ApiResponse.ok(postService.getPosts(page, size, worldId, null, characterId));
	}

	@PostMapping("/api/admin/worlds")
	public ResponseEntity<ApiResponse<IdResponse>> createWorld(@Valid @RequestBody WorldUpsertRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(worldService.createWorld(request)));
	}

	@PatchMapping("/api/admin/worlds/{worldId}")
	public ApiResponse<IdResponse> updateWorld(@PathVariable Long worldId, @Valid @RequestBody WorldUpsertRequest request) {
		return ApiResponse.ok(worldService.updateWorld(worldId, request));
	}

	@DeleteMapping("/api/admin/worlds/{worldId}")
	public ApiResponse<Void> deleteWorld(@PathVariable Long worldId) {
		worldService.deleteWorld(worldId);
		return ApiResponse.ok();
	}

	@GetMapping("/api/worlds/{worldId}/prompts")
	public ApiResponse<List<PromptListItem>> getPrompts(@PathVariable Long worldId) {
		return ApiResponse.ok(worldService.getPrompts(worldId));
	}

	@GetMapping("/api/worlds/{worldId}/prompts/{promptId}")
	public ApiResponse<PromptDetailResponse> getPrompt(@PathVariable Long worldId, @PathVariable Long promptId) {
		return ApiResponse.ok(worldService.getPrompt(worldId, promptId));
	}

	@PostMapping("/api/worlds/{worldId}/prompts")
	public ResponseEntity<ApiResponse<IdResponse>> createPrompt(@PathVariable Long worldId, @Valid @RequestBody PromptUpsertRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(worldService.createPrompt(worldId, request)));
	}

	@PatchMapping("/api/worlds/{worldId}/prompts/{promptId}")
	public ApiResponse<?> updatePrompt(@PathVariable Long worldId, @PathVariable Long promptId, @Valid @RequestBody PromptUpsertRequest request) {
		return ApiResponse.ok(worldService.updatePrompt(worldId, promptId, request));
	}

	@DeleteMapping("/api/worlds/{worldId}/prompts/{promptId}")
	public ApiResponse<Void> deletePrompt(@PathVariable Long worldId, @PathVariable Long promptId) {
		worldService.deletePrompt(worldId, promptId);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/worlds/{worldId}/prompts/sort")
	public ApiResponse<Void> updatePromptSort(@PathVariable Long worldId, @RequestBody PromptOrderRequest request) {
		worldService.updatePromptSort(worldId, request);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/worlds/{worldId}/prompts/{promptId}/active")
	public ApiResponse<Void> updatePromptActive(@PathVariable Long worldId, @PathVariable Long promptId, @RequestBody PromptActiveRequest request) {
		worldService.updatePromptActive(worldId, promptId, request);
		return ApiResponse.ok();
	}
}
