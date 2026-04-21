package com.mollubook.domain.community.controller;

import com.mollubook.domain.community.dto.CommunityDtos.CommunityDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.CommunityListItem;
import com.mollubook.domain.community.dto.CommunityDtos.CreateCommunityRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptActiveRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptUpsertRequest;
import com.mollubook.domain.community.dto.CommunityDtos.UpdateCommunityRequest;
import com.mollubook.domain.community.dto.CommunityDtos.VersionedIdResponse;
import com.mollubook.domain.community.service.CommunityService;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunityController {

	private final CommunityService communityService;

	public CommunityController(CommunityService communityService) {
		this.communityService = communityService;
	}

	@GetMapping("/api/communities")
	public ApiResponse<List<CommunityListItem>> getCommunities() {
		return ApiResponse.ok(communityService.getCommunities());
	}

	@GetMapping("/api/communities/{slug}")
	public ApiResponse<CommunityDetailResponse> getCommunity(@PathVariable String slug) {
		return ApiResponse.ok(communityService.getCommunity(slug));
	}

	@PostMapping("/api/admin/communities")
	public ResponseEntity<ApiResponse<IdResponse>> createCommunity(@Valid @RequestBody CreateCommunityRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(communityService.createCommunity(request)));
	}

	@PatchMapping("/api/admin/communities/{communityId}")
	public ApiResponse<IdResponse> updateCommunity(@PathVariable Long communityId, @Valid @RequestBody UpdateCommunityRequest request) {
		return ApiResponse.ok(communityService.updateCommunity(communityId, request));
	}

	@DeleteMapping("/api/admin/communities/{communityId}")
	public ApiResponse<Void> deleteCommunity(@PathVariable Long communityId) {
		communityService.deleteCommunity(communityId);
		return ApiResponse.ok();
	}

	@GetMapping("/api/communities/{communityId}/prompts")
	public ApiResponse<List<PromptListItem>> getPrompts(@PathVariable Long communityId) {
		return ApiResponse.ok(communityService.getPrompts(communityId));
	}

	@GetMapping("/api/communities/{communityId}/prompts/{promptId}")
	public ApiResponse<PromptDetailResponse> getPrompt(@PathVariable Long communityId, @PathVariable Long promptId) {
		return ApiResponse.ok(communityService.getPrompt(communityId, promptId));
	}

	@PostMapping("/api/communities/{communityId}/prompts")
	public ResponseEntity<ApiResponse<IdResponse>> createPrompt(@PathVariable Long communityId, @Valid @RequestBody PromptUpsertRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(communityService.createPrompt(communityId, request)));
	}

	@PatchMapping("/api/communities/{communityId}/prompts/{promptId}")
	public ApiResponse<VersionedIdResponse> updatePrompt(@PathVariable Long communityId, @PathVariable Long promptId, @Valid @RequestBody PromptUpsertRequest request) {
		return ApiResponse.ok(communityService.updatePrompt(communityId, promptId, request));
	}

	@DeleteMapping("/api/communities/{communityId}/prompts/{promptId}")
	public ApiResponse<Void> deletePrompt(@PathVariable Long communityId, @PathVariable Long promptId) {
		communityService.deletePrompt(communityId, promptId);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/communities/{communityId}/prompts/sort")
	public ApiResponse<Void> updatePromptSort(@PathVariable Long communityId, @RequestBody PromptOrderRequest request) {
		communityService.updatePromptSort(communityId, request);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/communities/{communityId}/prompts/{promptId}/active")
	public ApiResponse<Void> updatePromptActive(@PathVariable Long communityId, @PathVariable Long promptId, @RequestBody PromptActiveRequest request) {
		communityService.updatePromptActive(communityId, promptId, request);
		return ApiResponse.ok();
	}
}
