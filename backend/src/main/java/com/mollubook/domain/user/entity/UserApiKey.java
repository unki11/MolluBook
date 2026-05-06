package com.mollubook.domain.user.entity;

import com.mollubook.global.security.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_api_keys")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserApiKey extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "ai_model", nullable = false)
	private AiModel aiModel;

	@Column(nullable = false)
	private String label;

	@Column(name = "encrypted_key", nullable = false)
	private String encryptedKey;

	@Column(nullable = false, length = 1)
	private String isActive;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private UseYn useYn;

	@Builder
	public UserApiKey(User user, AiModel aiModel, String label, String encryptedKey, String isActive, UseYn useYn) {
		this.user = user;
		this.aiModel = aiModel;
		this.label = label;
		this.encryptedKey = encryptedKey;
		this.isActive = isActive;
		this.useYn = useYn;
	}

	public void update(AiModel aiModel, String label) {
		this.aiModel = aiModel;
		this.label = label;
	}

	public void updateEncryptedKey(String encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	public void delete() {
		this.useYn = UseYn.N;
	}
}
