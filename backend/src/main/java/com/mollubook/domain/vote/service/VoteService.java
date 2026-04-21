package com.mollubook.domain.vote.service;

import com.mollubook.domain.comment.entity.Comment;
import com.mollubook.domain.comment.repository.CommentRepository;
import com.mollubook.domain.post.dto.PostDtos.VoteRequest;
import com.mollubook.domain.post.dto.PostDtos.VoteResponse;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.user.entity.User;
import com.mollubook.domain.user.repository.UserRepository;
import com.mollubook.domain.vote.entity.CommentVote;
import com.mollubook.domain.vote.entity.PostVote;
import com.mollubook.domain.vote.entity.VoteType;
import com.mollubook.domain.vote.repository.CommentVoteRepository;
import com.mollubook.domain.vote.repository.PostVoteRepository;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class VoteService {

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final PostVoteRepository postVoteRepository;
	private final CommentVoteRepository commentVoteRepository;
	private final UserRepository userRepository;

	public VoteService(PostRepository postRepository, CommentRepository commentRepository, PostVoteRepository postVoteRepository, CommentVoteRepository commentVoteRepository, UserRepository userRepository) {
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.postVoteRepository = postVoteRepository;
		this.commentVoteRepository = commentVoteRepository;
		this.userRepository = userRepository;
	}

	public VoteResponse votePost(Long postId, VoteRequest request) {
		Post post = postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.COMMON_002));
		User user = currentUser();
		VoteType myVote = togglePostVote(user, post, request.voteType());
		int likeCount = postVoteRepository.countByPostIdAndVoteType(postId, VoteType.LIKE);
		int dislikeCount = postVoteRepository.countByPostIdAndVoteType(postId, VoteType.DISLIKE);
		post.applyVoteCounts(likeCount, dislikeCount);
		return new VoteResponse(likeCount, dislikeCount, myVote);
	}

	public VoteResponse voteComment(Long commentId, VoteRequest request) {
		Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new CustomException(ErrorCode.COMMON_002));
		User user = currentUser();
		VoteType myVote = toggleCommentVote(user, comment, request.voteType());
		int likeCount = commentVoteRepository.countByCommentIdAndVoteType(commentId, VoteType.LIKE);
		int dislikeCount = commentVoteRepository.countByCommentIdAndVoteType(commentId, VoteType.DISLIKE);
		comment.applyVoteCounts(likeCount, dislikeCount);
		return new VoteResponse(likeCount, dislikeCount, myVote);
	}

	public VoteType getPostVote(Long userId, Long postId) {
		if (userId == null) {
			return null;
		}
		return postVoteRepository.findByUserIdAndPostId(userId, postId).map(PostVote::getVoteType).orElse(null);
	}

	public VoteType getCommentVote(Long userId, Long commentId) {
		if (userId == null) {
			return null;
		}
		return commentVoteRepository.findByUserIdAndCommentId(userId, commentId).map(CommentVote::getVoteType).orElse(null);
	}

	private VoteType togglePostVote(User user, Post post, VoteType voteType) {
		return postVoteRepository.findByUserIdAndPostId(user.getId(), post.getId())
			.map(existing -> {
				if (existing.getVoteType() == voteType) {
					postVoteRepository.delete(existing);
					return (VoteType) null;
				}
				existing.updateVoteType(voteType);
				return voteType;
			})
			.orElseGet(() -> {
				postVoteRepository.save(PostVote.builder().user(user).post(post).voteType(voteType).build());
				return voteType;
			});
	}

	private VoteType toggleCommentVote(User user, Comment comment, VoteType voteType) {
		return commentVoteRepository.findByUserIdAndCommentId(user.getId(), comment.getId())
			.map(existing -> {
				if (existing.getVoteType() == voteType) {
					commentVoteRepository.delete(existing);
					return (VoteType) null;
				}
				existing.updateVoteType(voteType);
				return voteType;
			})
			.orElseGet(() -> {
				commentVoteRepository.save(CommentVote.builder().user(user).comment(comment).voteType(voteType).build());
				return voteType;
			});
	}

	private User currentUser() {
		return userRepository.findById(SecurityUtils.currentUser().id())
			.orElseThrow(() -> new CustomException(ErrorCode.USER_001));
	}
}
