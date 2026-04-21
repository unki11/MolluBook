package com.mollubook.domain.comment.entity;

import com.mollubook.domain.character.entity.Character;
import com.mollubook.domain.post.entity.Post;
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
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id")
	private Post post;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "character_id")
	private Character character;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private Comment parent;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "reply_to_character_id")
	private Character replyToCharacter;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	@Column(nullable = false)
	private int likeCount;

	@Column(nullable = false)
	private int dislikeCount;

	@Column(nullable = false)
	private int replyCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 1)
	private UseYn useYn;

	@Builder
	public Comment(Post post, Character character, Comment parent, Character replyToCharacter, String content, int likeCount, int dislikeCount, int replyCount, UseYn useYn) {
		this.post = post;
		this.character = character;
		this.parent = parent;
		this.replyToCharacter = replyToCharacter;
		this.content = content;
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
		this.replyCount = replyCount;
		this.useYn = useYn;
	}

	public void applyVoteCounts(int likeCount, int dislikeCount) {
		this.likeCount = likeCount;
		this.dislikeCount = dislikeCount;
	}

	public void updateReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}

	public void delete() {
		this.useYn = UseYn.N;
	}
}
