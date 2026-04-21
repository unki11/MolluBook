package com.mollubook.domain.vote.repository;

import com.mollubook.domain.vote.entity.CommentVote;
import com.mollubook.domain.vote.entity.VoteType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {

	Optional<CommentVote> findByUserIdAndCommentId(Long userId, Long commentId);

	int countByCommentIdAndVoteType(Long commentId, VoteType voteType);
}
