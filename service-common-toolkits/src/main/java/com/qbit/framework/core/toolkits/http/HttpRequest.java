package com.qbit.framework.core.toolkits.http;

import lombok.Getter;

import java.time.Duration;
import java.util.*;

/**
 * HTTP 请求对象，支持链式调用
 *
 * @author zhoubobing
 * @date 2026/1/7
 */
@Getter
public class HttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final Map<String, String> queryParams;
    private final Object body;
    private final Duration connectTimeout;
    private final Duration readTimeout;
    private final Duration writeTimeout;
    private final boolean followRedirects;
    private final String contentType;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.queryParams = Collections.unmodifiableMap(builder.queryParams);
        this.body = builder.body;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.writeTimeout = builder.writeTimeout;
        this.followRedirects = builder.followRedirects;
        this.contentType = builder.contentType;
    }

    public static Builder builder(String url) {
        return new Builder(url);
    }

    public static class Builder {
        private final String url;
        private String method = "GET";
        private final Map<String, String> headers = new LinkedHashMap<>();
        private final Map<String, String> queryParams = new LinkedHashMap<>();
        private Object body;
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private Duration writeTimeout = Duration.ofSeconds(30);
        private boolean followRedirects = true;
        private String contentType = "application/json";

        private Builder(String url) {
            this.url = Objects.requireNonNull(url, "URL cannot be null");
        }

        public Builder method(String method) {
            this.method = Objects.requireNonNull(method, "Method cannot be null").toUpperCase();
            return this;
        }

        public Builder get() {
            return method("GET");
        }

        public Builder post() {
            return method("POST");
        }

        public Builder put() {
            return method("PUT");
        }

        public Builder delete() {
            return method("DELETE");
        }

        public Builder patch() {
            return method("PATCH");
        }

        public Builder header(String name, String value) {
            this.headers.put(name, value);
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            if (headers != null) {
                this.headers.putAll(headers);
            }
            return this;
        }

        public Builder queryParam(String name, String value) {
            this.queryParams.put(name, value);
            return this;
        }

        public Builder queryParams(Map<String, String> params) {
            if (params != null) {
                this.queryParams.putAll(params);
            }
            return this;
        }

        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        public Builder jsonBody(Object body) {
            this.body = body;
            this.contentType = "application/json";
            return this;
        }

        public Builder formBody(Map<String, String> formData) {
            this.body = formData;
            this.contentType = "application/x-www-form-urlencoded";
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = timeout;
            return this;
        }

        public Builder readTimeout(Duration timeout) {
            this.readTimeout = timeout;
            return this;
        }

        public Builder writeTimeout(Duration timeout) {
            this.writeTimeout = timeout;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.connectTimeout = timeout;
            this.readTimeout = timeout;
            this.writeTimeout = timeout;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    /**
     * 获取完整的请求URL（包含查询参数）
     */
    public String getFullUrl() {
        if (queryParams.isEmpty()) {
            return url;
        }

        StringBuilder fullUrl = new StringBuilder(url);
        if (!url.contains("?")) {
            fullUrl.append("?");
        } else if (!url.endsWith("&")) {
            fullUrl.append("&");
        }

        StringJoiner joiner = new StringJoiner("&");
        queryParams.forEach((key, value) -> {
            joiner.add(urlEncode(key) + "=" + urlEncode(value));
        });
        fullUrl.append(joiner.toString());

        return fullUrl.toString();
    }

    private String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
