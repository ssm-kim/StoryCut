package com.storycut.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

/**
 * AES 암호화/복호화 유틸리티
 */
@Component
public class AesEncryptionUtil {

    private final Key secretKey;
    private static final String ALGORITHM = "AES";

    public AesEncryptionUtil(@Value("${app.encryption.key:defaultencryptionkey1234567890abcd}") String secretKeyString) {
        // 비밀키는 16, 24, 또는 32바이트여야 함 (AES-128, AES-192, AES-256)
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            byte[] adjustedKeyBytes = new byte[32]; // AES-256 사용
            System.arraycopy(keyBytes, 0, adjustedKeyBytes, 0, Math.min(keyBytes.length, 32));
            keyBytes = adjustedKeyBytes;
        }
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 문자열 암호화
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("암호화 실패", e);
        }
    }

    /**
     * 문자열 복호화
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedData)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("복호화 실패", e);
        }
    }
}
