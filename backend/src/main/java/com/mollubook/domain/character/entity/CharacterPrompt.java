package com.mollubook.domain.character.entity;

import com.mollubook.domain.user.entity.UseYn;
import com.mollubook.domain.user.entity.User;
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
@Table(name = "character_prompts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CharacterPrompt extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "character_id")
	private Character character;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "created_by")
	private User createdBy;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private boolean isPublic;

	@Column(nullable = false)
	private boolean isActive;

	@Column(nullable = false)
	private int version;

	@Column(nullable = false)
	private int sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private UseYn useYn;

	@Builder
	public CharacterPrompt(Character character, User createdBy, String title, String content, boolean isPublic, boolean isActive, int version, int sortOrder, UseYn useYn) {
		this.character = character;
		this.createdBy = createdBy;
		this.title = title;
		this.content = content;
		this.isPublic = isPublic;
		this.isActive = isActive;
		this.version = version;
		this.sortOrder = sortOrder;
		this.useYn = useYn;
	}

	public void update(String title, String content, boolean isPublic) {
		this.title = title;
		this.content = content;
		this.isPublic = isPublic;
		this.version += 1;
	}

	public void updateSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public void updateActive(boolean active) {
		isActive = active;
	}

	public void delete() {
		this.useYn = UseYn.N;
	}
}
