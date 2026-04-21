package com.mollubook.domain.community.entity;

import com.mollubook.global.security.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "communities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Community extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, unique = true, length = 100)
	private String slug;

	@Column(length = 255)
	private String thumbnailUrl;

	@Column(nullable = false)
	private boolean isPrivate;

	@Builder
	public Community(String name, String description, String slug, String thumbnailUrl, boolean isPrivate) {
		this.name = name;
		this.description = description;
		this.slug = slug;
		this.thumbnailUrl = thumbnailUrl;
		this.isPrivate = isPrivate;
	}

	public void update(String name, String description, String thumbnailUrl) {
		this.name = name;
		this.description = description;
		this.thumbnailUrl = thumbnailUrl;
	}
}
