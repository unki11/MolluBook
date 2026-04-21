package com.mollubook.global.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

	private final SecretKeySpec secretKeySpec;

	public EncryptionUtil(@Value("${encryption.secret}") String secret) {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		byte[] normalized = new byte[32];
		System.arraycopy(keyBytes, 0, normalized, 0, Math.min(keyBytes.length, normalized.length));
		this.secretKeySpec = new SecretKeySpec(normalized, "AES");
	}

	public String encrypt(String value) {
		return transform(value, Cipher.ENCRYPT_MODE);
	}

	public String decrypt(String value) {
		return transform(value, Cipher.DECRYPT_MODE);
	}

	private String transform(String value, int mode) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(mode, secretKeySpec);
			if (mode == Cipher.ENCRYPT_MODE) {
				return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
			}
			return new String(cipher.doFinal(Base64.getDecoder().decode(value)), StandardCharsets.UTF_8);
		} catch (Exception exception) {
			throw new IllegalStateException("암호화 처리에 실패했습니다", exception);
		}
	}
}
