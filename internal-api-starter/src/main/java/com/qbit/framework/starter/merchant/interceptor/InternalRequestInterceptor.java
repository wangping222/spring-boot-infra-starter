package com.qbit.framework.starter.merchant.interceptor;

import com.qbit.framework.starter.merchant.properties.FeignApiProperties;
import com.qbit.framework.common.encrypt.ShaUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


/**
 * 内部API请求拦截器
 */
public class InternalRequestInterceptor implements RequestInterceptor {
    private final FeignApiProperties properties;

    public InternalRequestInterceptor(FeignApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void apply(RequestTemplate template) {
        String method = template.method();
        if (StringUtils.equalsIgnoreCase(method, "GET")) {
            String qs = buildQueryString(template.queries());
            String toSign = (StringUtils.isBlank(qs) ? "" : qs + "&") + "sign=";
            String sign = ShaUtil.encrypt(toSign, properties.getSecret());
            template.query("sign", sign);
        } else {
            String path = template.path();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nonceStr = UUID.randomUUID().toString().replace("-", "");
            String message = method.toUpperCase() + path + timestamp + nonceStr;
            String sign = ShaUtil.encrypt(message, properties.getSecret());
            template.header("x-sign", sign);
            template.header("x-nonce-str", nonceStr);
            template.header("x-timestamp", timestamp);
        }
    }

    private String buildQueryString(Map<String, Collection<String>> queries) {
        if (queries == null || queries.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Collection<String>> e : queries.entrySet()) {
            String key = e.getKey();
            Collection<String> values = e.getValue();
            if (values == null || values.isEmpty()) {
                parts.add(key + "=");
                continue;
            }
            for (String v : values) {
                parts.add(key + "=" + v);
            }
        }
        return String.join("&", parts);
    }
}
