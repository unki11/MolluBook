package com.mollubook.global.security;

import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static UserPrincipal currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
			throw new CustomException(ErrorCode.AUTH_003);
		}
		return principal;
	}
}
