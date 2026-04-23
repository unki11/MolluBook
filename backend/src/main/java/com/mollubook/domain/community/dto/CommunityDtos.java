package com.mollubook.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public final class CommunityDtos {

	private CommunityDtos() {
	}

	public record CreateCommunityRequest(
		Long worldId,
		@NotBlank @Size(max = 100) String name,
		@NotBlank @Size(max = 100) String slug,
		String description,
		String thumbnailUrl
	) {
	}

	public record UpdateCommunityRequest(
		Long worldId,
		@NotBlank @Size(max = 100) String name,
		String description,
		String thumbnailUrl
	) {
	}

	public record CommunityListItem(
		Long id,
		String name,
		String slug,
		String description,
		String thumbnailUrl,
		WorldSummary world,
		long characterCount,
		long postCount
	) {
	}

	public record WorldSummary(Long id, String name, String slug) {
	}

	public record CharacterSummary(
		Long id,
		String name,
		int postCount,
		String status,
		LocalDateTime lastPostAt
	) {
	}

	public record CommunityDetailResponse(
		Long id,
		String name,
		String slug,
		String description,
		String thumbnailUrl,
		WorldSummary world,
		List<CharacterSummary> characters
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

	public record PromptListItem(
		Long id,
		String title,
		String content,
		boolean isActive,
		boolean isPublic,
		int version,
		int sortOrder,
		LocalDateTime createdAt
	) {
	}

	public record PromptCreator(Long id, String nickname) {
	}

	public record PromptDetailResponse(
		Long id,
		String title,
		String content,
		boolean isActive,
		boolean isPublic,
		int version,
		int sortOrder,
		PromptCreator createdBy,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
	) {
	}

	public record VersionedIdResponse(Long id, Integer version) {
	}
}
