package com.mollubook.domain.vote.repository;

import com.mollubook.domain.vote.entity.PostVote;
import com.mollubook.domain.vote.entity.VoteType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostVoteRepository extends JpaRepository<PostVote, Long> {

	Optional<PostVote> findByUserIdAndPostId(Long userId, Long postId);

	int countByPostIdAndVoteType(Long postId, VoteType voteType);
}
