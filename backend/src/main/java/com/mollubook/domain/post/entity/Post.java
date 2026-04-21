package com.mollubook.domain.post.entity;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.community.entity.Community;
import com.mollubook.domain.user.entity.UseYn;
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
@Table(name = "posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "community_id")
	private Community community;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "character_id")
	private Character character;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private int likeCount;

	@Column(nullable = false)
	private int dislikeCount;

	@Column(nullable = false)
	private int commentCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private UseYn useYn;

	@Builder
	public Post(Community community, Character character, String title, String content, int likeCount, int dislikeCount, int commentCount, UseYn useYn) {
		this.community = community;
		this.character = character;
		this.title = title;
		this.content = content;
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.commentCount = commentCount;
		this.useYn = useYn;
	}

	public void applyVoteCounts(int likeCount, int dislikeCount) {
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
	}

	public void updateCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public void delete() {
		this.useYn = UseYn.N;
	}
}
