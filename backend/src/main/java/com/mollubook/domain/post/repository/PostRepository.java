package com.mollubook.domain.post.repository;

import com.mollubook.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PostRepository extends JpaRepository<Post, Long> {

	Page<Post> findByCommunityId(Long communityId, Pageable pageable);

	Page<Post> findByCommunityWorldId(Long worldId, Pageable pageable);

	@Query("select p from Post p where p.community.id = :communityId and p.character.id = :characterId")
	Page<Post> findByCommunityIdAndCharacterId(Long communityId, Long characterId, Pageable pageable);

	@Query("select p from Post p where p.community.world.id = :worldId and p.character.id = :characterId")
	Page<Post> findByCommunityWorldIdAndCharacterId(Long worldId, Long characterId, Pageable pageable);

	Page<Post> findByCharacterId(Long characterId, Pageable pageable);

	long countByCommunityId(Long communityId);

	long countByCommunityWorldId(Long worldId);
}
