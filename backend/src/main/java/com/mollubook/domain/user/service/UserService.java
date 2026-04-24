package com.mollubook.domain.user.service;

import com.mollubook.domain.user.dto.AuthDtos.IdResponse;
import com.mollubook.domain.user.dto.AuthDtos.MeResponse;
import com.mollubook.domain.user.dto.AuthDtos.PasswordUpdateRequest;
import com.mollubook.domain.user.dto.AuthDtos.UpdateMeRequest;
import com.mollubook.domain.user.dto.AuthDtos.WithdrawRequest;
import com.mollubook.domain.user.dto.UserApiKeyDtos.UserApiKeyCreateRequest;
import com.mollubook.domain.user.dto.UserApiKeyDtos.UserApiKeyListItem;
import com.mollubook.domain.user.entity.OAuthProvider;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.entity.UserApiKey;
import com.mollubook.domain.user.entity.UserOauth;
import com.mollubook.domain.user.repository.UserApiKeyRepository;
import com.mollubook.domain.user.repository.UserOauthRepository;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import com.mollubook.global.util.EncryptionUtil;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserService {

	private final UserRepository userRepository;
	private final UserOauthRepository userOauthRepository;
	private final UserApiKeyRepository userApiKeyRepository;
	private final PasswordEncoder passwordEncoder;
	private final EncryptionUtil encryptionUtil;

	public UserService(UserRepository userRepository, UserOauthRepository userOauthRepository, UserApiKeyRepository userApiKeyRepository, PasswordEncoder passwordEncoder, EncryptionUtil encryptionUtil) {
		this.userRepository = userRepository;
		this.userOauthRepository = userOauthRepository;
		this.userApiKeyRepository = userApiKeyRepository;
		this.passwordEncoder = passwordEncoder;
		this.encryptionUtil = encryptionUtil;
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

	public List<UserApiKeyListItem> getMyApiKeys() {
		User user = currentUserEntity();
		return userApiKeyRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
			.map(apiKey -> new UserApiKeyListItem(
				apiKey.getId(),
				apiKey.getLabel(),
				apiKey.getAiModel(),
				"Y".equals(apiKey.getIsActive()),
				maskKey(encryptionUtil.decrypt(apiKey.getEncryptedKey())),
				apiKey.getCreatedAt()
			))
			.toList();
	}

	public IdResponse createApiKey(UserApiKeyCreateRequest request) {
		User user = currentUserEntity();
		UserApiKey apiKey = userApiKeyRepository.save(UserApiKey.builder()
			.user(user)
			.aiModel(request.aiModel())
			.label(request.label().trim())
			.encryptedKey(encryptionUtil.encrypt(request.apiKey().trim()))
			.isActive("Y")
			.build());
		return new IdResponse(apiKey.getId());
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

	private String maskKey(String rawKey) {
		if (rawKey == null || rawKey.isBlank()) {
			return "••••";
		}
		String trimmed = rawKey.trim();
		int visibleLength = Math.min(4, trimmed.length());
		return "••••" + trimmed.substring(trimmed.length() - visibleLength);
	}
}
