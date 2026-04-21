package com.mollubook.global.exception;

import com.mollubook.global.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustom(CustomException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		return ResponseEntity.status(errorCode.status())
			.body(ApiResponse.fail(errorCode.code(), errorCode.message()));
	}

	@ExceptionHandler({
		MethodArgumentNotValidException.class,
		ConstraintViolationException.class,
		IllegalArgumentException.class
	})
	public ResponseEntity<ApiResponse<Void>> handleBadRequest(Exception exception) {
		return ResponseEntity.badRequest()
			.body(ApiResponse.fail(ErrorCode.COMMON_002.code(), exception.getMessage()));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDenied() {
		return ResponseEntity.status(ErrorCode.COMMON_001.status())
			.body(ApiResponse.fail(ErrorCode.COMMON_001.code(), ErrorCode.COMMON_001.message()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnhandled(Exception exception) {
		return ResponseEntity.internalServerError()
			.body(ApiResponse.fail("INTERNAL_SERVER_ERROR", exception.getMessage()));
	}
}
