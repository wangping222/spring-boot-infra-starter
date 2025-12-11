package com.qbit.framework.starter.merchant.interceptor;

import com.qbit.framework.core.api.model.toolkits.encrypt.ShaUtil;
import com.qbit.framework.starter.merchant.properties.FeignApiProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 内部 API 请求签名拦截器
 * <p>
 * 职责：为 Feign 请求添加签名相关的请求头或查询参数
 * <ul>
 * <li>GET 请求：添加 sign 查询参数</li>
 * <li>其他请求：添加 x-sign、x-nonce-str、x-timestamp 请求头</li>
 * </ul>
 *
 * @author internal-api-starter
 */
public class InternalRequestInterceptor implements RequestInterceptor {

    private static final String HEADER_SIGN = "x-sign";
    private static final String HEADER_NONCE_STR = "x-nonce-str";
    private static final String HEADER_TIMESTAMP = "x-timestamp";
    private static final String QUERY_SIGN = "sign";

    private final FeignApiProperties properties;

    public InternalRequestInterceptor(FeignApiProperties properties) {
        this.properties = Objects.requireNonNull(properties, "FeignApiProperties must not be null");
    }

    @Override
    public void apply(RequestTemplate template) {
        String method = template.method();
        if (StringUtils.isBlank(method)) {
            return;
        }

        if (StringUtils.equalsIgnoreCase(method, "GET")) {
            applyGetSignature(template);
        } else {
            applyPostSignature(template);
        }
    }

    /**
     * 为 GET 请求添加签名查询参数
     */
    private void applyGetSignature(RequestTemplate template) {
        String queryString = buildQueryString(template.queries());
        String toSign = (StringUtils.isBlank(queryString) ? "" : queryString + "&") + QUERY_SIGN + "=";
        String sign = ShaUtil.encrypt(toSign, properties.getSecret());
        template.query(QUERY_SIGN, sign);
    }

    /**
     * 为非 GET 请求添加签名请求头
     */
    private void applyPostSignature(RequestTemplate template) {
        String path = template.path();
        if (StringUtils.isBlank(path)) {
            path = "";
        }

        String method = template.method();
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonceStr = UUID.randomUUID().toString().replace("-", "");
        String message = method.toUpperCase() + path + timestamp + nonceStr;
        String sign = ShaUtil.encrypt(message, properties.getSecret());

        template.header(HEADER_SIGN, sign);
        template.header(HEADER_NONCE_STR, nonceStr);
        template.header(HEADER_TIMESTAMP, timestamp);
    }

    /**
     * 构建查询字符串
     *
     * @param queries 查询参数 Map
     * @return 查询字符串，格式：key1=value1&key2=value2
     */
    private String buildQueryString(Map<String, Collection<String>> queries) {
        if (queries == null || queries.isEmpty()) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Collection<String>> entry : queries.entrySet()) {
            String key = entry.getKey();
            Collection<String> values = entry.getValue();

            if (values == null || values.isEmpty()) {
                parts.add(key + "=");
                continue;
            }

            for (String value : values) {
                parts.add(key + "=" + value);
            }
        }

        return String.join("&", parts);
    }
}
