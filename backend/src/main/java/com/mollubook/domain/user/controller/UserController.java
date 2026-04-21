package com.mollubook.domain.user.controller;

import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.dto.AuthDtos.MeResponse;
import com.mollubook.domain.user.dto.AuthDtos.PasswordUpdateRequest;
import com.mollubook.domain.user.dto.AuthDtos.UpdateMeRequest;
import com.mollubook.domain.user.dto.AuthDtos.WithdrawRequest;
import com.mollubook.domain.user.service.UserService;
import com.mollubook.global.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public ApiResponse<MeResponse> me() {
		return ApiResponse.ok(userService.getMe());
	}

	@PatchMapping("/me")
	public ApiResponse<IdResponse> updateMe(@Valid @RequestBody UpdateMeRequest request) {
		return ApiResponse.ok(userService.updateMe(request));
	}

	@PatchMapping("/me/password")
	public ApiResponse<Void> updatePassword(@Valid @RequestBody PasswordUpdateRequest request) {
		userService.updatePassword(request);
		return ApiResponse.ok();
	}

	@DeleteMapping("/me")
	public ApiResponse<Void> withdraw(@RequestBody(required = false) WithdrawRequest request) {
		userService.withdraw(request);
		return ApiResponse.ok();
	}
}
