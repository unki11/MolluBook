package com.mollubook.domain.user.repository;

import com.mollubook.domain.user.entity.UserApiKey;
import com.mollubook.domain.user.entity.UseYn;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserApiKeyRepository extends JpaRepository<UserApiKey, Long> {

	List<UserApiKey> findByUserIdAndUseYnOrderByCreatedAtDesc(Long userId, UseYn useYn);

	Optional<UserApiKey> findByIdAndUseYn(Long id, UseYn useYn);
}
