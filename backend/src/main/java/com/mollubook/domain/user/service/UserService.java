package com.mollubook.domain.user.service;

import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.dto.AuthDtos.MeResponse;
import com.mollubook.domain.user.dto.AuthDtos.PasswordUpdateRequest;
import com.mollubook.domain.user.dto.AuthDtos.UpdateMeRequest;
import com.mollubook.domain.user.dto.AuthDtos.WithdrawRequest;
import com.mollubook.domain.user.entity.OAuthProvider;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.entity.UserOauth;
import com.mollubook.domain.user.repository.UserOauthRepository;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final UserOauthRepository userOauthRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, UserOauthRepository userOauthRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userOauthRepository = userOauthRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public MeResponse getMe() {
		User user = currentUserEntity();
		String provider = userOauthRepository.findFirstByUserId(user.getId())
			.map(UserOauth::getProvider)
			.orElse(OAuthProvider.LOCAL)
			.name();
		return new MeResponse(user.getId(), user.getEmail(), user.getNickname(), user.getSystemRole(), provider, user.getPassword() != null, user.getCreatedAt());
	}

	public IdResponse updateMe(UpdateMeRequest request) {
		User user = currentUserEntity();
		user.updateNickname(request.nickname());
		return new IdResponse(user.getId());
	}

	public void updatePassword(PasswordUpdateRequest request) {
		User user = currentUserEntity();
		if (user.getPassword() != null) {
			if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
				throw new CustomException(ErrorCode.AUTH_001);
			}
		}
		user.updatePassword(passwordEncoder.encode(request.newPassword()));
	}

	public void withdraw(WithdrawRequest request) {
		User user = currentUserEntity();
		if (user.getPassword() != null && (request == null || request.password() == null || !passwordEncoder.matches(request.password(), user.getPassword()))) {
			throw new CustomException(ErrorCode.AUTH_001);
		}
		user.deactivate();
	}

	public User currentUserEntity() {
		return userRepository.findById(SecurityUtils.currentUser().id())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_001));
	}
}
