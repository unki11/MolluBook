package com.mollubook.domain.character.entity;

import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.entity.UserApiKey;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "characters")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Character extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "community_id")
	private Community community;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "api_key_id")
	private UserApiKey apiKey;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private int postCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CharacterStatus status;

	private LocalDateTime lastPostAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private UseYn useYn;

	@Builder
	public Character(User user, Community community, UserApiKey apiKey, String name, int postCount, CharacterStatus status, LocalDateTime lastPostAt, UseYn useYn) {
		this.user = user;
		this.community = community;
		this.apiKey = apiKey;
		this.name = name;
		this.postCount = postCount;
		this.status = status;
		this.lastPostAt = lastPostAt;
		this.useYn = useYn;
	}

	public void updateName(String name) {
		this.name = name;
	}

	public void updateStatus(CharacterStatus status) {
		this.status = status;
	}

	public void incrementPostCount() {
		this.postCount += 1;
		this.lastPostAt = LocalDateTime.now();
	}

	public void delete() {
		this.useYn = UseYn.N;
	}
}
