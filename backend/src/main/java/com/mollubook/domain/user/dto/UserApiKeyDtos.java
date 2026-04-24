package com.mollubook.domain.user.dto;

import com.mollubook.domain.user.entity.AiModel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class UserApiKeyDtos {

	private UserApiKeyDtos() {
	}

	public record UserApiKeyCreateRequest(
		@NotBlank @Size(max = 100) String label,
		@NotBlank String apiKey,
		@NotNull AiModel aiModel
	) {
	}

	public record UserApiKeyListItem(
		Long id,
		String label,
		AiModel aiModel,
		boolean isActive,
		String maskedKey,
		LocalDateTime createdAt
	) {
	}
}
