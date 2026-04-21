package com.mollubook.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mollubook.global.exception.CustomException;
import com.mollubook.global.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith("Bearer ")) {
			try {
				UserPrincipal principal = jwtTokenProvider.parse(authorization.substring(7));
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					principal,
					null,
					List.of(new SimpleGrantedAuthority("ROLE_" + principal.systemRole().name()))
				);
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (CustomException exception) {
				response.setStatus(exception.getErrorCode().status().value());
				response.setContentType(MediaType.APPLICATION_JSON_VALUE);
				response.setCharacterEncoding("UTF-8");
				response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(
					exception.getErrorCode().code(),
					exception.getErrorCode().message()
				)));
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
}
