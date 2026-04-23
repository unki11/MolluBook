package com.mollubook.domain.world.repository;

import com.mollubook.domain.world.entity.WorldPrompt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldPromptRepository extends JpaRepository<WorldPrompt, Long> {

	List<WorldPrompt> findByWorldIdOrderBySortOrderAsc(Long worldId);
}
