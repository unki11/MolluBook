package com.mollubook.domain.comment.service;

import com.mollubook.domain.comment.dto.CommentDtos.CharacterRef;
import com.mollubook.domain.comment.dto.CommentDtos.CommentReply;
import com.mollubook.domain.comment.dto.CommentDtos.CommentThread;
import com.mollubook.domain.comment.entity.Comment;
import com.mollubook.domain.comment.repository.CommentRepository;
import com.mollubook.domain.vote.service.VoteService;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.security.SecurityUtils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

	private final CommentRepository commentRepository;
	private final VoteService voteService;

	public CommentService(CommentRepository commentRepository, VoteService voteService) {
		this.commentRepository = commentRepository;
		this.voteService = voteService;
	}

	@Transactional(readOnly = true)
	public List<CommentThread> getComments(Long postId) {
		List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
		Long userId = null;
		try {
			userId = SecurityUtils.currentUser().id();
		} catch (CustomException ignored) {
		}
		final Long currentUserId = userId;
		return comments.stream()
			.filter(comment -> comment.getParent() == null)
			.map(parent -> new CommentThread(
				parent.getId(),
				parent.getContent(),
				parent.getLikeCount(),
				parent.getDislikeCount(),
				voteService.getCommentVote(currentUserId, parent.getId()),
				parent.getCreatedAt(),
				new CharacterRef(parent.getCharacter().getId(), parent.getCharacter().getName()),
				comments.stream()
					.filter(reply -> reply.getParent() != null && reply.getParent().getId().equals(parent.getId()))
					.map(reply -> new CommentReply(
						reply.getId(),
						reply.getContent(),
						reply.getLikeCount(),
						reply.getDislikeCount(),
						voteService.getCommentVote(currentUserId, reply.getId()),
						reply.getCreatedAt(),
						new CharacterRef(reply.getCharacter().getId(), reply.getCharacter().getName()),
						reply.getReplyToCharacter() == null ? null : new CharacterRef(reply.getReplyToCharacter().getId(), reply.getReplyToCharacter().getName())
					))
					.toList()
			))
			.toList();
	}

	public void deleteComment(Long commentId) {
		commentRepository.findById(commentId)
			.orElseThrow(() -> new CustomException(com.mollubook.global.exception.ErrorCode.COMMON_002))
			.delete();
	}
}
