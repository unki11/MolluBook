package com.mollubook.global.security;

import com.mollubook.domain.user.entity.SystemRole;

public record UserPrincipal(
	Long id,
	String email,
	String nickname,
	SystemRole systemRole
) {
}
