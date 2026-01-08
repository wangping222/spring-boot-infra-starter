package com.qbit.framework.core.toolkits.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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

    // ==================== 便捷的 JSON 解析方法 ====================

    /**
     * 将响应体解析为指定类型的对象
     *
     * @param clazz 目标类型
     * @param <T> 类型参数
     * @return 解析后的对象
     * @throws HttpClientException 如果响应不成功或解析失败
     */
    public <T> T as(Class<T> clazz) {
        checkSuccessful();
        try {
            return JSON.parseObject(body, clazz);
        } catch (Exception e) {
            throw new HttpClientException("Failed to parse response body to " + clazz.getSimpleName(), e);
        }
    }

    /**
     * 将响应体解析为指定类型（支持泛型）
     *
     * @param typeReference 类型引用
     * @param <T> 类型参数
     * @return 解析后的对象
     * @throws HttpClientException 如果响应不成功或解析失败
     */
    public <T> T as(TypeReference<T> typeReference) {
        checkSuccessful();
        try {
            return JSON.parseObject(body, typeReference);
        } catch (Exception e) {
            throw new HttpClientException("Failed to parse response body", e);
        }
    }

    /**
     * 将响应体解析为 List
     *
     * @param elementType List 元素类型
     * @param <T> 元素类型参数
     * @return 解析后的 List
     * @throws HttpClientException 如果响应不成功或解析失败
     */
    public <T> List<T> asList(Class<T> elementType) {
        checkSuccessful();
        try {
            return JSON.parseArray(body, elementType);
        } catch (Exception e) {
            throw new HttpClientException("Failed to parse response body to List<" + elementType.getSimpleName() + ">", e);
        }
    }

    /**
     * 将响应体解析为 Map
     *
     * @return 解析后的 Map
     * @throws HttpClientException 如果响应不成功或解析失败
     */
    public Map<String, Object> asMap() {
        checkSuccessful();
        try {
            return JSON.parseObject(body, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new HttpClientException("Failed to parse response body to Map", e);
        }
    }

    /**
     * 安全地将响应体解析为指定类型，失败时返回 null
     *
     * @param clazz 目标类型
     * @param <T> 类型参数
     * @return 解析后的对象，失败返回 null
     */
    public <T> T asOrNull(Class<T> clazz) {
        if (!successful) {
            return null;
        }
        try {
            return JSON.parseObject(body, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全地将响应体解析为指定类型，失败时返回默认值
     *
     * @param clazz 目标类型
     * @param defaultValue 默认值
     * @param <T> 类型参数
     * @return 解析后的对象，失败返回默认值
     */
    public <T> T asOrDefault(Class<T> clazz, T defaultValue) {
        if (!successful) {
            return defaultValue;
        }
        try {
            return JSON.parseObject(body, clazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 检查响应是否成功，不成功则抛出异常
     */
    private void checkSuccessful() {
        if (!successful) {
            throw new HttpClientException("Request failed with status code: " + statusCode + ", body: " + body);
        }
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
