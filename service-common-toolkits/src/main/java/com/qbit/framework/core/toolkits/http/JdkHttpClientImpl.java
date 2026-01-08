package com.qbit.framework.core.toolkits.http;

import com.alibaba.fastjson.JSON;
import com.qbit.framework.core.toolkits.http.interceptor.HttpClientInterceptor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JDK HttpClient 实现（JDK 11+）
 *
 * @author Qbit Framework

 * @date 2026/1/7
 */
@Slf4j
public class JdkHttpClientImpl implements HttpClient {

    private final java.net.http.HttpClient httpClient;
    private final List<HttpClientInterceptor> interceptors = new ArrayList<>();

    /**
     * 使用默认配置构造
     */
    public JdkHttpClientImpl() {
        this(java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build());
    }

    /**
     * 使用自定义HttpClient构造
     */
    public JdkHttpClientImpl(java.net.http.HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "HttpClient cannot be null");
    }

    /**
     * 使用构建器创建
     */
    public static JdkHttpClientImpl create(java.net.http.HttpClient.Builder builder) {
        return new JdkHttpClientImpl(builder.build());
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws HttpClientException {
        try {
            // 应用拦截器
            HttpRequest processedRequest = applyInterceptors(request);

            // 构建JDK HttpRequest
            java.net.http.HttpRequest jdkRequest = buildJdkHttpRequest(processedRequest);

            // 执行请求
            java.net.http.HttpResponse<String> response = httpClient.send(jdkRequest, BodyHandlers.ofString());

            return buildHttpResponse(response);
        } catch (IOException e) {
            log.error("HTTP request failed: {} {}", request.getMethod(), request.getFullUrl(), e);
            throw new HttpClientException("HTTP request failed", e);
        } catch (InterruptedException e) {
            log.error("HTTP request interrupted: {} {}", request.getMethod(), request.getFullUrl(), e);
            Thread.currentThread().interrupt();
            throw new HttpClientException("HTTP request interrupted", e);
        } catch (Exception e) {
            log.error("Unexpected error during HTTP request: {} {}", request.getMethod(), request.getFullUrl(), e);
            throw new HttpClientException("Unexpected error during HTTP request", e);
        }
    }

    private java.net.http.HttpRequest buildJdkHttpRequest(HttpRequest request) {
        java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(request.getFullUrl()))
                .timeout(request.getReadTimeout());

        // 添加headers
        request.getHeaders().forEach(builder::header);

        // 添加Content-Type（如果没有手动设置）
        if (!request.getHeaders().containsKey("Content-Type") && request.getContentType() != null) {
            builder.header("Content-Type", request.getContentType());
        }

        // 构建请求体
        java.net.http.HttpRequest.BodyPublisher bodyPublisher = buildBodyPublisher(request);

        // 设置方法和请求体
        builder.method(request.getMethod(), bodyPublisher);

        return builder.build();
    }

    private java.net.http.HttpRequest.BodyPublisher buildBodyPublisher(HttpRequest request) {
        if (request.getBody() == null) {
            // GET、DELETE等可能没有请求体
            if ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod())) {
                return BodyPublishers.noBody();
            }
            return BodyPublishers.ofString("");
        }

        // 根据Content-Type处理不同类型的body
        if (request.getContentType().contains("application/json")) {
            String json;
            if (request.getBody() instanceof String) {
                json = (String) request.getBody();
            } else {
                json = JSON.toJSONString(request.getBody());
            }
            return BodyPublishers.ofString(json);
        } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
            if (request.getBody() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> formData = (Map<String, String>) request.getBody();
                String formBody = formData.entrySet().stream()
                        .map(entry -> urlEncode(entry.getKey()) + "=" + urlEncode(entry.getValue()))
                        .collect(Collectors.joining("&"));
                return BodyPublishers.ofString(formBody);
            }
            return BodyPublishers.ofString("");
        } else if (request.getBody() instanceof String) {
            return BodyPublishers.ofString((String) request.getBody());
        } else {
            // 其他类型转JSON
            return BodyPublishers.ofString(JSON.toJSONString(request.getBody()));
        }
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    private HttpResponse buildHttpResponse(java.net.http.HttpResponse<String> response) {
        int statusCode = response.statusCode();
        String body = response.body();

        // 转换headers
        HttpHeaders jdkHeaders = response.headers();
        Map<String, List<String>> headers = jdkHeaders.map();

        boolean successful = statusCode >= 200 && statusCode < 300;

        return new HttpResponse(statusCode, body, headers, successful);
    }

    @Override
    public HttpClient addInterceptor(HttpClientInterceptor interceptor) {
        if (interceptor != null) {
            this.interceptors.add(interceptor);
            // 按优先级排序
            this.interceptors.sort(Comparator.comparingInt(HttpClientInterceptor::getOrder));
        }
        return this;
    }

    @Override
    public List<HttpClientInterceptor> getInterceptors() {
        return Collections.unmodifiableList(interceptors);
    }

    /**
     * 应用所有拦截器
     */
    private HttpRequest applyInterceptors(HttpRequest request) {
        HttpRequest result = request;
        for (HttpClientInterceptor interceptor : interceptors) {
            result = interceptor.intercept(result);
        }
        return result;
    }

    @Override
    public void close() {
        // JDK HttpClient 不需要显式关闭
    }
}
