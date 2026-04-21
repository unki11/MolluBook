package com.mollubook.domain.character.repository;

import com.mollubook.domain.character.entity.CharacterPrompt;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterPromptRepository extends JpaRepository<CharacterPrompt, Long> {

	List<CharacterPrompt> findByCharacterIdOrderBySortOrderAsc(Long characterId);
}
