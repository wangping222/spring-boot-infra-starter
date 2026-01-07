package com.qbit.framework.core.toolkits.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * HTTP 响应对象
 *
 * @author zhoubobing
 * @date 2026/1/7
 */
@Getter
@AllArgsConstructor
public class HttpResponse {
    private final int statusCode;
    private final String body;
    private final Map<String, List<String>> headers;
    private final boolean successful;

    /**
     * 判断响应是否成功（状态码 2xx）
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * 获取指定header的值（第一个）
     */
    public String getHeader(String name) {
        List<String> values = headers.get(name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    /**
     * 获取指定header的所有值
     */
    public List<String> getHeaders(String name) {
        return headers.getOrDefault(name, Collections.emptyList());
    }

    /**
     * 获取响应体长度
     */
    public int getContentLength() {
        return body != null ? body.length() : 0;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", successful=" + successful +
                ", bodyLength=" + getContentLength() +
                '}';
    }
}
