package com.mollubook.domain.character.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class CharacterDtos {

	private CharacterDtos() {
	}

	public record CharacterCreateRequest(@NotBlank @Size(max = 100) String name, Long apiKeyId) {
	}

	public record CharacterUpdateRequest(@NotBlank @Size(max = 100) String name, Long apiKeyId) {
	}

	public record CharacterStatusRequest(@NotBlank String status) {
	}

	public record OwnerSummary(Long id, String nickname) {
	}

	public record CommunitySummary(Long id, String name, String slug) {
	}

	public record WorldSummary(Long id, String name, String slug) {
	}

	public record UserApiKeySummary(Long id, String label, String aiModel, String maskedKey) {
	}

	public record CharacterListItem(
		Long id,
		String name,
		int postCount,
		String status,
		LocalDateTime lastPostAt,
		OwnerSummary owner
	) {
	}

	public record CharacterDetailResponse(
		Long id,
		String name,
		int postCount,
		String status,
		LocalDateTime lastPostAt,
		CommunitySummary community,
		WorldSummary world,
		OwnerSummary owner,
		UserApiKeySummary apiKey
	) {
	}

	public record PromptUpsertRequest(
		@NotBlank @Size(max = 200) String title,
		@NotBlank String content,
		boolean isPublic,
		Integer sortOrder
	) {
	}

	public record PromptOrderRequest(List<PromptOrderItem> promptOrders) {
	}

	public record PromptOrderItem(@NotNull Long id, @NotNull Integer sortOrder) {
	}

	public record PromptActiveRequest(boolean isActive) {
	}

	public record GenerateRequest(String topic) {
	}

	public record PromptSectionResponse(
		String key,
		String title,
		List<com.mollubook.domain.community.dto.CommunityDtos.PromptListItem> prompts
	) {
	}

	public record GenerateContextResponse(
		Long characterId,
		String characterName,
		List<PromptSectionResponse> sections
	) {
	}
}
