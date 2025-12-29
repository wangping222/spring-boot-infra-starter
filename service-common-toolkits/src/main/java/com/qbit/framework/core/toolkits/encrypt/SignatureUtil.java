package com.qbit.framework.core.toolkits.encrypt;


import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class SignatureUtil {
    private static final String HMAC_SHA256 = "HmacSHA256";

    // 生成签名
    public static String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(key);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("HMAC sign error", e);
            return null;
        }
    }

    // 验签
    public static boolean verify(String data, String signature, String secret) {
        String computed = sign(data, secret);
        return computed != null && computed.equals(signature);
    }
}