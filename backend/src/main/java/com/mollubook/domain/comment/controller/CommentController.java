package com.mollubook.domain.comment.controller;

import com.mollubook.domain.comment.dto.CommentDtos.CommentThread;
import com.mollubook.domain.comment.service.CommentService;
import com.mollubook.domain.post.dto.PostDtos.VoteRequest;
import com.mollubook.domain.post.dto.PostDtos.VoteResponse;
import com.mollubook.domain.vote.service.VoteService;
import com.mollubook.global.response.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

	private final CommentService commentService;
	private final VoteService voteService;

	public CommentController(CommentService commentService, VoteService voteService) {
		this.commentService = commentService;
		this.voteService = voteService;
	}

	@GetMapping("/api/posts/{postId}/comments")
	public ApiResponse<List<CommentThread>> getComments(@PathVariable Long postId) {
		return ApiResponse.ok(commentService.getComments(postId));
	}

	@PostMapping("/api/comments/{commentId}/vote")
	public ApiResponse<VoteResponse> voteComment(@PathVariable Long commentId, @RequestBody VoteRequest request) {
		return ApiResponse.ok(voteService.voteComment(commentId, request));
	}

	@DeleteMapping("/api/admin/comments/{commentId}")
	public ApiResponse<Void> deleteComment(@PathVariable Long commentId) {
		commentService.deleteComment(commentId);
		return ApiResponse.ok();
	}
}
