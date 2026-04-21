package com.mollubook.domain.community.repository;

import com.mollubook.domain.community.entity.CommunityPrompt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPromptRepository extends JpaRepository<CommunityPrompt, Long> {

	List<CommunityPrompt> findByCommunityIdOrderBySortOrderAsc(Long communityId);
}
