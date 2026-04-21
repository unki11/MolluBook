package com.mollubook.domain.comment.dto;

import com.mollubook.domain.vote.entity.VoteType;
import java.time.LocalDateTime;
import java.util.List;

public final class CommentDtos {

	private CommentDtos() {
	}

	public record CharacterRef(Long id, String name) {
	}

	public record CommentReply(
		Long id,
		String content,
		int likeCount,
		int dislikeCount,
		VoteType myVote,
		LocalDateTime createdAt,
		CharacterRef character,
		CharacterRef replyToCharacter
	) {
	}

	public record CommentThread(
		Long id,
		String content,
		int likeCount,
		int dislikeCount,
		VoteType myVote,
		LocalDateTime createdAt,
		CharacterRef character,
		List<CommentReply> replies
	) {
	}
}
