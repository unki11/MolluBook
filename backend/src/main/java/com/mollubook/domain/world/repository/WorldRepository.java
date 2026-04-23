package com.mollubook.domain.world.repository;

import com.mollubook.domain.world.entity.World;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldRepository extends JpaRepository<World, Long> {

	Optional<World> findBySlug(String slug);
}
