package com.qbit.framework.business.service.starter.encrypt;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * @author john
 * {@code @date}  2025/10/11 18:00
 * {@code @description}
 **/
public class EncryptUtil {
    public static String decrypt(String secretContent, String secretKey) {
        try {
            byte[] key = secretKey.getBytes(StandardCharsets.UTF_8);
            byte[] cipherBytes = HexFormat.of().parseHex(secretContent);

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] plainBytes = cipher.doFinal(cipherBytes);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt error", e);
        }
    }
}
