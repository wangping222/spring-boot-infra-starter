package com.qbit.framework.core.toolkits.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.time.Duration;
import java.util.Map;

/**
 * HTTP 工具类 - 提供简化的静态方法
 * 默认使用 JDK HttpClient 实现
 *
 * @author zhoubobing
 * @date 2026/1/7
 */
public class HttpUtils {

    private static final HttpClient DEFAULT_CLIENT = new JdkHttpClientImpl();

    private HttpUtils() {
    }

    // ==================== GET 请求 ====================

    /**
     * 发送GET请求
     */
    public static HttpResponse get(String url) {
        return get(url, null, null);
    }

    /**
     * 发送GET请求（带请求头）
     */
    public static HttpResponse get(String url, Map<String, String> headers) {
        return get(url, headers, null);
    }

    /**
     * 发送GET请求（带请求头和查询参数）
     */
    public static HttpResponse get(String url, Map<String, String> headers, Map<String, String> queryParams) {
        HttpRequest.Builder builder = HttpRequest.builder(url).get();

        if (headers != null) {
            builder.headers(headers);
        }
        if (queryParams != null) {
            builder.queryParams(queryParams);
        }

        return DEFAULT_CLIENT.execute(builder.build());
    }

    // ==================== POST 请求 ====================

    /**
     * 发送POST请求（JSON body）
     */
    public static HttpResponse post(String url, Object body) {
        return post(url, null, body);
    }

    /**
     * 发送POST请求（带请求头，JSON body）
     */
    public static HttpResponse post(String url, Map<String, String> headers, Object body) {
        HttpRequest.Builder builder = HttpRequest.builder(url)
                .post()
                .jsonBody(body);

        if (headers != null) {
            builder.headers(headers);
        }

        return DEFAULT_CLIENT.execute(builder.build());
    }

    /**
     * 发送POST表单请求
     */
    public static HttpResponse postForm(String url, Map<String, String> formData) {
        return postForm(url, null, formData);
    }

    /**
     * 发送POST表单请求（带请求头）
     */
    public static HttpResponse postForm(String url, Map<String, String> headers, Map<String, String> formData) {
        HttpRequest.Builder builder = HttpRequest.builder(url)
                .post()
                .formBody(formData);

        if (headers != null) {
            builder.headers(headers);
        }

        return DEFAULT_CLIENT.execute(builder.build());
    }

    // ==================== PUT 请求 ====================

    /**
     * 发送PUT请求（JSON body）
     */
    public static HttpResponse put(String url, Object body) {
        return put(url, null, body);
    }

    /**
     * 发送PUT请求（带请求头，JSON body）
     */
    public static HttpResponse put(String url, Map<String, String> headers, Object body) {
        HttpRequest.Builder builder = HttpRequest.builder(url)
                .put()
                .jsonBody(body);

        if (headers != null) {
            builder.headers(headers);
        }

        return DEFAULT_CLIENT.execute(builder.build());
    }

    // ==================== DELETE 请求 ====================

    /**
     * 发送DELETE请求
     */
    public static HttpResponse delete(String url) {
        return delete(url, null);
    }

    /**
     * 发送DELETE请求（带请求头）
     */
    public static HttpResponse delete(String url, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.builder(url).delete();

        if (headers != null) {
            builder.headers(headers);
        }

        return DEFAULT_CLIENT.execute(builder.build());
    }

    // ==================== JSON 解析辅助方法 ====================

    /**
     * 解析响应为指定类型
     */
    public static <T> T parseResponse(HttpResponse response, Class<T> clazz) {
        if (!response.isSuccessful()) {
            throw new HttpClientException("Request failed with status: " + response.getStatusCode());
        }
        return JSON.parseObject(response.getBody(), clazz);
    }

    /**
     * 解析响应为指定类型（支持泛型）
     */
    public static <T> T parseResponse(HttpResponse response, TypeReference<T> typeReference) {
        if (!response.isSuccessful()) {
            throw new HttpClientException("Request failed with status: " + response.getStatusCode());
        }
        return JSON.parseObject(response.getBody(), typeReference);
    }

    // ==================== 高级用法 ====================

    /**
     * 使用自定义客户端执行请求
     */
    public static HttpResponse execute(HttpClient client, HttpRequest request) {
        return client.execute(request);
    }

    /**
     * 创建请求构建器
     */
    public static HttpRequest.Builder request(String url) {
        return HttpRequest.builder(url);
    }

    /**
     * 获取默认客户端
     */
    public static HttpClient getDefaultClient() {
        return DEFAULT_CLIENT;
    }

    /**
     * 创建OkHttp客户端
     */
    public static HttpClient createOkHttpClient() {
        return new OkHttpClientImpl();
    }

    /**
     * 创建JDK HttpClient
     */
    public static HttpClient createJdkHttpClient() {
        return new JdkHttpClientImpl();
    }

    /**
     * 创建带超时配置的客户端
     */
    public static HttpClient createClientWithTimeout(Duration timeout) {
        return new JdkHttpClientImpl(
                java.net.http.HttpClient.newBuilder()
                        .connectTimeout(timeout)
                        .build()
        );
    }
}
