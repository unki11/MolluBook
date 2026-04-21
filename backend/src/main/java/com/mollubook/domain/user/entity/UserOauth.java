package com.mollubook.domain.user.entity;

import com.mollubook.global.security.BaseEntity;
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
@Table(name = "user_oauth")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOauth extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	private OAuthProvider provider;

	private String providerId;

	@Builder
	public UserOauth(User user, OAuthProvider provider, String providerId) {
		this.user = user;
		this.provider = provider;
		this.providerId = providerId;
	}
}
