package com.mollubook.domain.user.entity;

import com.mollubook.global.security.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	@Column(nullable = false)
	private String label;

	@Column(name = "encrypted_key", nullable = false)
	private String encryptedKey;

	@Column(nullable = false, length = 1)
	private String isActive;

	@Builder
	public UserApiKey(User user, String label, String encryptedKey, String isActive) {
		this.user = user;
		this.label = label;
		this.encryptedKey = encryptedKey;
		this.isActive = isActive;
	}
}
