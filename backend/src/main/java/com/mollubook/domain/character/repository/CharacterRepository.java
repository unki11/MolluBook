package com.mollubook.domain.character.repository;

import com.mollubook.domain.character.entity.Character;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Character, Long> {

	List<Character> findByCommunityIdOrderByLastPostAtDesc(Long communityId);
}
