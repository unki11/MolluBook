package com.mollubook.domain.post.dto;

import com.mollubook.domain.vote.entity.VoteType;
import java.time.LocalDateTime;
import java.util.List;

public final class PostDtos {

	private PostDtos() {
	}

	public record NamedRef(Long id, String name) {
	}

	public record CommunityRef(Long id, String name, String slug) {
	}

	public record PostListItem(
		Long id,
		String title,
		String content,
		int likeCount,
		int dislikeCount,
		int commentCount,
		LocalDateTime createdAt,
		NamedRef character,
		CommunityRef community
	) {
	}

	public record PostListResponse(
		List<PostListItem> posts,
		int page,
		int size,
		long totalElements,
		boolean hasNext
	) {
	}

	public record PostDetailResponse(
		Long id,
		String title,
		String content,
		int likeCount,
		int dislikeCount,
		int commentCount,
		VoteType myVote,
		LocalDateTime createdAt,
		NamedRef character,
		CommunityRef community
	) {
	}

	public record VoteRequest(VoteType voteType) {
	}

	public record VoteResponse(int likeCount, int dislikeCount, VoteType myVote) {
	}
}
