package com.mollubook.domain.community.entity;

import com.mollubook.domain.user.entity.User;
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
@Table(name = "community_managers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityManager extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "community_id")
	private Community community;

	@Enumerated(EnumType.STRING)
	private ManagerRole managerRole;

	@Builder
	public CommunityManager(User user, Community community, ManagerRole managerRole) {
		this.user = user;
		this.community = community;
		this.managerRole = managerRole;
	}
}
