package com.mollubook.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mollubook.global.response.ApiResponse;
import com.mollubook.global.security.JwtAuthenticationFilter;
import com.mollubook.global.security.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;
	private final List<String> allowedOrigins;

	public SecurityConfig(
		JwtAuthenticationFilter jwtAuthenticationFilter,
		ObjectMapper objectMapper,
		@Value("${app.cors.allowed-origins:http://localhost:5173,http://127.0.0.1:5173}") List<String> allowedOrigins
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.objectMapper = objectMapper;
		this.allowedOrigins = allowedOrigins;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/swagger-ui.html",
					"/swagger-ui/**",
					"/v3/api-docs/**",
					"/h2-console/**",
					"/api/auth/**"
				).permitAll()
				.requestMatchers(HttpMethod.GET, "/api/worlds", "/api/worlds/*", "/api/worlds/*/communities", "/api/worlds/*/posts", "/api/communities", "/api/communities/*", "/api/communities/*/posts", "/api/communities/*/characters", "/api/posts/**", "/api/comments/**", "/api/characters/*").permitAll()
				.requestMatchers("/api/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated()
			)
			.oauth2Login(Customizer.withDefaults())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(configurer -> configurer
				.authenticationEntryPoint((request, response, authException) -> writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "AUTH_003", "Authentication required"))
				.accessDeniedHandler((request, response, accessDeniedException) -> writeError(response, HttpServletResponse.SC_FORBIDDEN, "COMMON_001", "Access denied"))
			)
			.headers(headers -> headers.frameOptions(frame -> frame.disable()))
			.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(allowedOrigins);
		configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setMaxAge(3600L);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	private void writeError(HttpServletResponse response, int status, String code, String message) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
	}
}
