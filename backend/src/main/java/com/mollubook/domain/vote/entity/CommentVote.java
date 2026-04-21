package com.mollubook.domain.vote.entity;

import com.mollubook.domain.comment.entity.Comment;
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
@Table(name = "comment_votes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentVote extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "comment_id")
	private Comment comment;

	@Enumerated(EnumType.STRING)
	private VoteType voteType;

	@Builder
	public CommentVote(User user, Comment comment, VoteType voteType) {
		this.user = user;
		this.comment = comment;
		this.voteType = voteType;
	}

	public void updateVoteType(VoteType voteType) {
		this.voteType = voteType;
	}
}
