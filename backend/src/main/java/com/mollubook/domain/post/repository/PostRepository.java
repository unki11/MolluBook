package com.mollubook.domain.post.repository;

import com.mollubook.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findByCommunityId(Long communityId, Pageable pageable);

	Page<Post> findByCharacterId(Long characterId, Pageable pageable);

	long countByCommunityId(Long communityId);
}
