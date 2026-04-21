package com.mollubook.domain.user.service;

import com.mollubook.domain.user.dto.AuthDtos.AuthTokenResponse;
import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.dto.AuthDtos.LoginRequest;
import com.mollubook.domain.user.dto.AuthDtos.RefreshRequest;
import com.mollubook.domain.user.dto.AuthDtos.SignUpRequest;
import com.mollubook.domain.user.dto.AuthDtos.UserSummary;
import com.mollubook.domain.user.entity.OAuthProvider;
import com.mollubook.domain.user.entity.SystemRole;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.entity.UserOauth;
import com.mollubook.domain.user.repository.UserOauthRepository;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.JwtTokenProvider;
import com.mollubook.global.security.UserPrincipal;
import jakarta.transaction.Transactional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {

	private final UserRepository userRepository;
	private final UserOauthRepository userOauthRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(UserRepository userRepository, UserOauthRepository userOauthRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
		this.userRepository = userRepository;
		this.userOauthRepository = userOauthRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	public IdResponse signUp(SignUpRequest request) {
		if (userRepository.existsByEmail(request.email())) {
			throw new CustomException(ErrorCode.AUTH_004);
		}

		User user = userRepository.save(User.builder()
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.nickname(request.nickname())
			.systemRole(SystemRole.USER)
			.useYn(UseYn.Y)
			.build());
		return new IdResponse(user.getId());
	}

	public AuthTokenResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(request.email())
			.orElseThrow(() -> new CustomException(ErrorCode.AUTH_001));
		if (user.getPassword() == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
			throw new CustomException(ErrorCode.AUTH_001);
		}
		return issueToken(user, false);
	}

	public AuthTokenResponse refresh(RefreshRequest request) {
		UserPrincipal principal = jwtTokenProvider.parse(request.refreshToken());
		User user = userRepository.findById(principal.id())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_001));
		return issueToken(user, false);
	}

	public AuthTokenResponse mockGoogleCallback() {
		String email = "google_" + UUID.randomUUID().toString().substring(0, 8) + "@molubook.dev";
		User user = User.builder()
			.email(email)
			.nickname("구글유저")
			.password(null)
			.systemRole(SystemRole.USER)
			.useYn(UseYn.Y)
			.build();
		User savedUser = userRepository.save(user);
		userOauthRepository.save(UserOauth.builder()
			.user(savedUser)
			.provider(OAuthProvider.GOOGLE)
			.providerId(UUID.randomUUID().toString())
			.build());
		return issueToken(savedUser, true);
	}

	private AuthTokenResponse issueToken(User user, boolean isNewUser) {
		UserPrincipal principal = new UserPrincipal(user.getId(), user.getEmail(), user.getNickname(), user.getSystemRole());
		return new AuthTokenResponse(
			jwtTokenProvider.createAccessToken(principal),
			jwtTokenProvider.createRefreshToken(principal),
			new UserSummary(user.getId(), user.getNickname(), user.getSystemRole(), isNewUser)
		);
	}
}
