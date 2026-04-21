package com.mollubook.domain.community.repository;

import com.mollubook.domain.community.entity.CommunityManager;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityManagerRepository extends JpaRepository<CommunityManager, Long> {

	Optional<CommunityManager> findByUserIdAndCommunityId(Long userId, Long communityId);
}
