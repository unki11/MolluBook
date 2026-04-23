package com.mollubook.domain.world.dto;

import com.mollubook.domain.community.dto.CommunityDtos.PromptActiveRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptOrderRequest;
import com.mollubook.domain.community.dto.CommunityDtos.PromptUpsertRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class WorldDtos {

	private WorldDtos() {
	}

	public record WorldUpsertRequest(
		@NotBlank @Size(max = 100) String name,
		@Size(max = 100) String slug,
		String description,
		String thumbnailUrl
	) {
	}

	public record WorldListItem(
		Long id,
		String name,
		String slug,
		String description,
		String thumbnailUrl,
		long communityCount
	) {
	}

	public record CommunitySummary(
		Long id,
		String name,
		String slug,
		long characterCount
	) {
	}

	public record WorldDetailResponse(
		Long id,
		String name,
		String slug,
		String description,
		String thumbnailUrl,
		List<CommunitySummary> communities
	) {
	}

	public record PromptRequests(PromptUpsertRequest upsert, PromptOrderRequest order, PromptActiveRequest active) {
	}
}
