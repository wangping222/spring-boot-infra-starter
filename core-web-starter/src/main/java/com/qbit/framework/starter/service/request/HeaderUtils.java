package com.qbit.framework.starter.service.request;

import com.qbit.framework.common.encrypt.ShaUtil;
import org.springframework.http.HttpHeaders;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;

/**
 * @description @Date 2025/9/25 15:38
 * @author zhoubobing
 */
public class HeaderUtils {

    public static HttpHeaders buildNodeHeaders(String accountId, String secret) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String message = String.format("accountId=%s&timestamp=%s", accountId, timestamp);
        String sign = ShaUtil.encrypt(message, secret);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("timestamp", timestamp);
        httpHeaders.add("sign", sign);
        httpHeaders.add("account-id", accountId);
        return httpHeaders;
    }

    /**
     * 构建带签名的请求头
     *
     * @param secret 签名密钥
     * @param method 请求方法（GET、POST、PUT、DELETE）
     * @param path 接口路径（必须和服务端验签逻辑一致，比如 "/api/core/rd/walletAccountDetail"）
     * @return HttpHeaders
     */
    public static HttpHeaders buildAssetsHeaders(
            String secret, String method, String path, String accountId) {
        SecureRandom random = new SecureRandom();
        String nonce = HexFormat.of().formatHex(random.generateSeed(8));
        long timestamp = System.currentTimeMillis();

        // 拼接 message：method + path + timestamp + nonce
        List<String> message =
                List.of(method.toUpperCase(), path, String.valueOf(timestamp), nonce);
        String messageStr = String.join("", message);

        // 生成签名
        String sign = ShaUtil.encrypt(messageStr, secret);

        // 构造 header
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("nonceStr", nonce);
        httpHeaders.add("timestamp", String.valueOf(timestamp));
        httpHeaders.add("sign", sign);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("account-id", accountId);
        return httpHeaders;
    }
}
