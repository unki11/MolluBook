package com.mollubook.domain.post.controller;

import com.mollubook.domain.post.dto.PostDtos.PostDetailResponse;
import com.mollubook.domain.post.dto.PostDtos.PostListResponse;
import com.mollubook.domain.post.dto.PostDtos.VoteRequest;
import com.mollubook.domain.post.dto.PostDtos.VoteResponse;
import com.mollubook.domain.post.service.PostService;
import com.mollubook.domain.vote.service.VoteService;
import com.mollubook.global.response.ApiResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PostController {

	private final PostService postService;
	private final VoteService voteService;

	public PostController(PostService postService, VoteService voteService) {
		this.postService = postService;
		this.voteService = voteService;
	}

	@GetMapping("/api/posts")
	public ApiResponse<PostListResponse> getPosts(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) Long characterId
	) {
		return ApiResponse.ok(postService.getPosts(page, size, null, characterId));
	}

	@GetMapping("/api/communities/{communityId}/posts")
	public ApiResponse<PostListResponse> getCommunityPosts(
		@PathVariable Long communityId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size,
		@RequestParam(required = false) String sort,
		@RequestParam(required = false) Long characterId
	) {
		return ApiResponse.ok(postService.getPosts(page, size, communityId, characterId));
	}

	@GetMapping("/api/posts/{postId}")
	public ApiResponse<PostDetailResponse> getPost(@PathVariable Long postId) {
		return ApiResponse.ok(postService.getPost(postId));
	}

	@PostMapping("/api/posts/{postId}/vote")
	public ApiResponse<VoteResponse> votePost(@PathVariable Long postId, @RequestBody VoteRequest request) {
		return ApiResponse.ok(voteService.votePost(postId, request));
	}

	@DeleteMapping("/api/admin/posts/{postId}")
	public ApiResponse<Void> deletePost(@PathVariable Long postId) {
		postService.deletePost(postId);
		return ApiResponse.ok();
	}
}
