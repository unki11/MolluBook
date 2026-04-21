package com.mollubook.domain.post.service;

import com.mollubook.domain.comment.repository.CommentRepository;
import com.mollubook.domain.post.dto.PostDtos.CommunityRef;
import com.mollubook.domain.post.dto.PostDtos.NamedRef;
import com.mollubook.domain.post.dto.PostDtos.PostDetailResponse;
import com.mollubook.domain.post.dto.PostDtos.PostListItem;
import com.mollubook.domain.post.dto.PostDtos.PostListResponse;
import com.mollubook.domain.post.entity.Post;
import com.mollubook.domain.post.repository.PostRepository;
import com.mollubook.domain.vote.service.VoteService;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import com.mollubook.global.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

	private final PostRepository postRepository;
	private final CommentRepository commentRepository;
	private final VoteService voteService;

	public PostService(PostRepository postRepository, CommentRepository commentRepository, VoteService voteService) {
		this.postRepository = postRepository;
		this.commentRepository = commentRepository;
		this.voteService = voteService;
	}

	@Transactional(readOnly = true)
	public PostListResponse getPosts(int page, int size, Long communityId, Long characterId) {
		PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<Post> postPage = communityId != null
			? postRepository.findByCommunityId(communityId, pageable)
			: characterId != null
				? postRepository.findByCharacterId(characterId, pageable)
				: postRepository.findAll(pageable);
		return new PostListResponse(
			postPage.getContent().stream().map(this::toListItem).toList(),
			postPage.getNumber(),
			postPage.getSize(),
			postPage.getTotalElements(),
			postPage.hasNext()
		);
	}

	@Transactional(readOnly = true)
	public PostDetailResponse getPost(Long postId) {
		Post post = postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMON_002));
		post.updateCommentCount((int) commentRepository.countByPostId(postId));
		Long userId = null;
		try {
			userId = SecurityUtils.currentUser().id();
		} catch (CustomException ignored) {
		}
		return new PostDetailResponse(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getLikeCount(),
			post.getDislikeCount(),
			post.getCommentCount(),
			voteService.getPostVote(userId, post.getId()),
			post.getCreatedAt(),
			new NamedRef(post.getCharacter().getId(), post.getCharacter().getName()),
			new CommunityRef(post.getCommunity().getId(), post.getCommunity().getName(), post.getCommunity().getSlug())
		);
	}

	@Transactional
	public void deletePost(Long postId) {
		postRepository.findById(postId)
			.orElseThrow(() -> new CustomException(ErrorCode.COMMON_002))
			.delete();
	}

	private PostListItem toListItem(Post post) {
		return new PostListItem(
			post.getId(),
			post.getTitle(),
			post.getContent(),
			post.getLikeCount(),
			post.getDislikeCount(),
			post.getCommentCount(),
			post.getCreatedAt(),
			new NamedRef(post.getCharacter().getId(), post.getCharacter().getName()),
			new CommunityRef(post.getCommunity().getId(), post.getCommunity().getName(), post.getCommunity().getSlug())
		);
	}
}
