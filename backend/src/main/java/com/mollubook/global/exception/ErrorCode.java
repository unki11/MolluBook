package com.mollubook.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH_001", "이메일 또는 비밀번호가 올바르지 않습니다"),
	AUTH_002(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다"),
	AUTH_003(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다"),
	AUTH_004(HttpStatus.CONFLICT, "AUTH_004", "이미 사용 중인 이메일입니다"),
	USER_001(HttpStatus.NOT_FOUND, "USER_001", "유저를 찾을 수 없습니다"),
	CHARACTER_001(HttpStatus.NOT_FOUND, "CHARACTER_001", "캐릭터를 찾을 수 없습니다"),
	CHARACTER_002(HttpStatus.FORBIDDEN, "CHARACTER_002", "캐릭터 수정/삭제 권한이 없습니다"),
	CHARACTER_003(HttpStatus.BAD_REQUEST, "CHARACTER_003", "정지된 캐릭터입니다"),
	PROMPT_001(HttpStatus.NOT_FOUND, "PROMPT_001", "프롬프트를 찾을 수 없습니다"),
	PROMPT_002(HttpStatus.FORBIDDEN, "PROMPT_002", "프롬프트 수정/삭제 권한이 없습니다"),
	COMMUNITY_001(HttpStatus.NOT_FOUND, "COMMUNITY_001", "커뮤니티를 찾을 수 없습니다"),
	COMMUNITY_002(HttpStatus.FORBIDDEN, "COMMUNITY_002", "커뮤니티 관리자 권한이 없습니다"),
	COMMON_001(HttpStatus.FORBIDDEN, "COMMON_001", "권한이 없습니다"),
	COMMON_002(HttpStatus.BAD_REQUEST, "COMMON_002", "잘못된 요청입니다");

	private final HttpStatus status;
	private final String code;
	private final String message;

	ErrorCode(HttpStatus status, String code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}

	public HttpStatus status() {
		return status;
	}

	public String code() {
		return code;
	}

	public String message() {
		return message;
	}
}
