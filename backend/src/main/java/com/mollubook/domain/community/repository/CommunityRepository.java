package com.mollubook.domain.community.repository;

import com.mollubook.domain.community.entity.Community;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {

	Optional<Community> findBySlug(String slug);
}
