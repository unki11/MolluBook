package com.mollubook.domain.user.dto;

import com.mollubook.domain.user.entity.SystemRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class AuthDtos {

	private AuthDtos() {
	}

	public record SignUpRequest(
		@Email @NotBlank String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@NotBlank @Size(max = 50) String nickname
	) {
	}

	public record LoginRequest(
		@Email @NotBlank String email,
		@NotBlank String password
	) {
	}

	public record RefreshRequest(
		@NotBlank String refreshToken
	) {
	}

	public record LogoutResponse() {
	}

	public record IdResponse(Long id) {
	}

	public record UserSummary(Long id, String nickname, SystemRole systemRole, Boolean isNewUser) {
	}

	public record AuthTokenResponse(String accessToken, String refreshToken, UserSummary user) {
	}

	public record MeResponse(
		Long id,
		String email,
		String nickname,
		SystemRole systemRole,
		String provider,
		boolean hasPassword,
		LocalDateTime createdAt
	) {
	}

	public record UpdateMeRequest(
		@NotBlank @Size(max = 50) String nickname
	) {
	}

	public record PasswordUpdateRequest(
		String currentPassword,
		@NotBlank @Size(min = 8, max = 100) String newPassword
	) {
	}

	public record WithdrawRequest(String password) {
	}
}
