package com.mollubook.domain.user.repository;

import com.mollubook.domain.user.entity.OAuthProvider;
import com.mollubook.domain.user.entity.UserOauth;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOauthRepository extends JpaRepository<UserOauth, Long> {

	Optional<UserOauth> findByProviderAndProviderId(OAuthProvider provider, String providerId);

	Optional<UserOauth> findFirstByUserId(Long userId);
}
