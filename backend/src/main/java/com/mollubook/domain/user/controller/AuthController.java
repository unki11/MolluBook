package com.mollubook.domain.user.controller;

import com.mollubook.domain.user.dto.AuthDtos.AuthTokenResponse;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.dto.AuthDtos.LoginRequest;
import com.mollubook.domain.user.dto.AuthDtos.RefreshRequest;
import com.mollubook.domain.user.dto.AuthDtos.SignUpRequest;
import com.mollubook.domain.user.service.AuthService;
import com.mollubook.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/signup")
	public ResponseEntity<ApiResponse<IdResponse>> signUp(@Valid @RequestBody SignUpRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(authService.signUp(request)));
	}

	@PostMapping("/login")
	public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok(authService.login(request));
	}

	@PostMapping("/refresh")
	public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
		return ApiResponse.ok(authService.refresh(request));
	}

	@PostMapping("/logout")
	public ApiResponse<Void> logout() {
		return ApiResponse.ok();
	}

	@GetMapping("/oauth2/google")
	public ApiResponse<String> oauthGoogle() {
		return ApiResponse.ok("/api/auth/oauth2/callback/google");
	}

	@GetMapping("/oauth2/callback/google")
	public ApiResponse<AuthTokenResponse> oauthGoogleCallback() {
		return ApiResponse.ok(authService.mockGoogleCallback());
	}
}
