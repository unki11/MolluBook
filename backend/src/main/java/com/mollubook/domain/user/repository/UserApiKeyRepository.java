package com.mollubook.domain.user.repository;

import com.mollubook.domain.user.entity.UserApiKey;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserApiKeyRepository extends JpaRepository<UserApiKey, Long> {

	List<UserApiKey> findByUserIdOrderByCreatedAtDesc(Long userId);
}
