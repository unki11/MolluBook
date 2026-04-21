package com.mollubook.global.security;

import com.mollubook.domain.user.entity.SystemRole;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	private final JwtProperties properties;
	private final SecretKey secretKey;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String createAccessToken(UserPrincipal principal) {
		return createToken(principal, properties.accessTokenExpirationSeconds(), "access");
	}

	public String createRefreshToken(UserPrincipal principal) {
		return createToken(principal, properties.refreshTokenExpirationSeconds(), "refresh");
	}

	public UserPrincipal parse(String token) {
		try {
			Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();

			return new UserPrincipal(
				Long.valueOf(claims.getSubject()),
				claims.get("email", String.class),
				claims.get("nickname", String.class),
				SystemRole.valueOf(claims.get("systemRole", String.class))
			);
		} catch (ExpiredJwtException exception) {
			throw new CustomException(ErrorCode.AUTH_002);
		} catch (JwtException | IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.AUTH_003);
		}
	}

	private String createToken(UserPrincipal principal, long expirationSeconds, String tokenType) {
		Instant now = Instant.now();
		return Jwts.builder()
			.issuer(properties.issuer())
			.subject(String.valueOf(principal.id()))
			.claim("email", principal.email())
			.claim("nickname", principal.nickname())
			.claim("systemRole", principal.systemRole().name())
			.claim("tokenType", tokenType)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plusSeconds(expirationSeconds)))
			.signWith(secretKey)
			.compact();
	}
}
