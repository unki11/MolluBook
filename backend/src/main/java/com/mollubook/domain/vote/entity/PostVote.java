package com.mollubook.domain.vote.entity;

import com.mollubook.domain.post.entity.Post;
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
@Table(name = "post_votes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostVote extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "post_id")
	private Post post;

	@Enumerated(EnumType.STRING)
	private VoteType voteType;

	@Builder
	public PostVote(User user, Post post, VoteType voteType) {
		this.user = user;
		this.post = post;
		this.voteType = voteType;
	}

	public void updateVoteType(VoteType voteType) {
		this.voteType = voteType;
	}
}
