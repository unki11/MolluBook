package com.mollubook.domain.user.entity;

import com.mollubook.global.security.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(length = 255)
	private String password;

	@Column(nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private SystemRole systemRole;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private UseYn useYn;

	@Builder
	public User(String email, String password, String nickname, SystemRole systemRole, UseYn useYn) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.systemRole = systemRole;
		this.useYn = useYn;
	}

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updatePassword(String password) {
		this.password = password;
	}

	public void deactivate() {
		this.useYn = UseYn.N;
	}
}
