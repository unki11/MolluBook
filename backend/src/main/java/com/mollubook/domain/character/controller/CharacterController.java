package com.mollubook.domain.character.controller;

import com.mollubook.domain.character.dto.CharacterDtos.CharacterCreateRequest;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterDetailResponse;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterListItem;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterStatusRequest;
import com.mollubook.domain.character.dto.CharacterDtos.CharacterUpdateRequest;
import com.mollubook.domain.character.dto.CharacterDtos.GenerateRequest;
import com.mollubook.domain.character.dto.CharacterDtos.GenerateContextResponse;
import com.mollubook.domain.character.dto.CharacterDtos.PromptActiveRequest;
import com.mollubook.domain.character.dto.CharacterDtos.PromptOrderRequest;
import com.mollubook.domain.character.dto.CharacterDtos.PromptUpsertRequest;
import com.mollubook.domain.character.service.CharacterService;
import com.mollubook.domain.character.service.GenerateService;
import com.mollubook.domain.community.dto.CommunityDtos.PromptDetailResponse;
import com.mollubook.domain.community.dto.CommunityDtos.PromptListItem;
import com.mollubook.domain.community.dto.CommunityDtos.VersionedIdResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CharacterController {

	private final CharacterService characterService;
	private final GenerateService generateService;

	public CharacterController(CharacterService characterService, GenerateService generateService) {
		this.characterService = characterService;
		this.generateService = generateService;
	}

	@GetMapping("/api/communities/{communityId}/characters")
	public ApiResponse<List<CharacterListItem>> getCharacters(@PathVariable Long communityId) {
		return ApiResponse.ok(characterService.getCharacters(communityId));
	}

	@GetMapping("/api/characters/{characterId}")
	public ApiResponse<CharacterDetailResponse> getCharacter(@PathVariable Long characterId) {
		return ApiResponse.ok(characterService.getCharacter(characterId));
	}

	@PostMapping("/api/communities/{communityId}/characters")
	public ResponseEntity<ApiResponse<IdResponse>> createCharacter(@PathVariable Long communityId, @Valid @RequestBody CharacterCreateRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(characterService.createCharacter(communityId, request)));
	}

	@PatchMapping("/api/characters/{characterId}")
	public ApiResponse<IdResponse> updateCharacter(@PathVariable Long characterId, @Valid @RequestBody CharacterUpdateRequest request) {
		return ApiResponse.ok(characterService.updateCharacter(characterId, request));
	}

	@DeleteMapping("/api/characters/{characterId}")
	public ApiResponse<Void> deleteCharacter(@PathVariable Long characterId) {
		characterService.deleteCharacter(characterId);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/admin/characters/{characterId}/status")
	public ApiResponse<Void> updateCharacterStatus(@PathVariable Long characterId, @RequestBody CharacterStatusRequest request) {
		characterService.updateStatus(characterId, request);
		return ApiResponse.ok();
	}

	@DeleteMapping("/api/admin/characters/{characterId}")
	public ApiResponse<Void> adminDeleteCharacter(@PathVariable Long characterId) {
		characterService.deleteCharacter(characterId);
		return ApiResponse.ok();
	}

	@GetMapping("/api/characters/{characterId}/prompts")
	public ApiResponse<List<PromptListItem>> getPrompts(@PathVariable Long characterId) {
		return ApiResponse.ok(characterService.getPrompts(characterId));
	}

	@GetMapping("/api/characters/{characterId}/prompts/{promptId}")
	public ApiResponse<PromptDetailResponse> getPrompt(@PathVariable Long characterId, @PathVariable Long promptId) {
		return ApiResponse.ok(characterService.getPrompt(characterId, promptId));
	}

	@PostMapping("/api/characters/{characterId}/prompts")
	public ResponseEntity<ApiResponse<IdResponse>> createPrompt(@PathVariable Long characterId, @Valid @RequestBody PromptUpsertRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(characterService.createPrompt(characterId, request)));
	}

	@PatchMapping("/api/characters/{characterId}/prompts/{promptId}")
	public ApiResponse<VersionedIdResponse> updatePrompt(@PathVariable Long characterId, @PathVariable Long promptId, @Valid @RequestBody PromptUpsertRequest request) {
		return ApiResponse.ok(characterService.updatePrompt(characterId, promptId, request));
	}

	@DeleteMapping("/api/characters/{characterId}/prompts/{promptId}")
	public ApiResponse<Void> deletePrompt(@PathVariable Long characterId, @PathVariable Long promptId) {
		characterService.deletePrompt(characterId, promptId);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/characters/{characterId}/prompts/sort")
	public ApiResponse<Void> updatePromptSort(@PathVariable Long characterId, @RequestBody PromptOrderRequest request) {
		characterService.updatePromptSort(characterId, request);
		return ApiResponse.ok();
	}

	@PatchMapping("/api/characters/{characterId}/prompts/{promptId}/active")
	public ApiResponse<Void> updatePromptActive(@PathVariable Long characterId, @PathVariable Long promptId, @RequestBody PromptActiveRequest request) {
		characterService.updatePromptActive(characterId, promptId, request);
		return ApiResponse.ok();
	}

	@PostMapping("/api/characters/{characterId}/generate")
	public ApiResponse<IdResponse> generate(@PathVariable Long characterId, @RequestBody(required = false) GenerateRequest request) {
		return ApiResponse.ok(generateService.generate(characterId, request == null ? null : request.topic()));
	}

	@GetMapping("/api/characters/{characterId}/generate/context")
	public ApiResponse<GenerateContextResponse> getGenerateContext(@PathVariable Long characterId) {
		return ApiResponse.ok(generateService.getGenerateContext(characterId));
	}

	@PostMapping("/api/characters/{characterId}/generate/manual")
	public ApiResponse<IdResponse> generateManual(@PathVariable Long characterId, @RequestBody(required = false) GenerateRequest request) {
		return ApiResponse.ok(generateService.generateManual(characterId, request == null ? null : request.topic()));
	}
}
