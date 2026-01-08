package com.qbit.framework.core.toolkits.http;

import com.alibaba.fastjson.JSON;
import com.qbit.framework.core.toolkits.http.interceptor.HttpClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp 实现的 HTTP 客户端
 *
 * @author zhoubobing
 * @date 2026/1/7
 */
@Slf4j
public class OkHttpClientImpl implements HttpClient {

    private final OkHttpClient okHttpClient;
    private final List<HttpClientInterceptor> interceptors = new ArrayList<>();

    /**
     * 使用默认配置构造
     */
    public OkHttpClientImpl() {
        this(new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build());
    }

    /**
     * 使用自定义OkHttpClient构造
     */
    public OkHttpClientImpl(OkHttpClient okHttpClient) {
        this.okHttpClient = Objects.requireNonNull(okHttpClient, "OkHttpClient cannot be null");
    }

    /**
     * 使用构建器创建
     */
    public static OkHttpClientImpl create(OkHttpClient.Builder builder) {
        return new OkHttpClientImpl(builder.build());
    }

    @Override
    public HttpResponse execute(HttpRequest request) throws HttpClientException {
        try {
            // 应用拦截器
            HttpRequest processedRequest = applyInterceptors(request);

            // 创建OkHttp客户端（针对每个请求的超时设置）
            OkHttpClient client = okHttpClient.newBuilder()
                    .connectTimeout(processedRequest.getConnectTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .readTimeout(processedRequest.getReadTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .writeTimeout(processedRequest.getWriteTimeout().toMillis(), TimeUnit.MILLISECONDS)
                    .followRedirects(processedRequest.isFollowRedirects())
                    .build();

            // 构建请求
            Request okRequest = buildOkHttpRequest(processedRequest);

            // 执行请求
            try (Response response = client.newCall(okRequest).execute()) {
                return buildHttpResponse(response);
            }
        } catch (IOException e) {
            log.error("HTTP request failed: {} {}", request.getMethod(), request.getFullUrl(), e);
            throw new HttpClientException("HTTP request failed", e);
        } catch (Exception e) {
            log.error("Unexpected error during HTTP request: {} {}", request.getMethod(), request.getFullUrl(), e);
            throw new HttpClientException("Unexpected error during HTTP request", e);
        }
    }

    private Request buildOkHttpRequest(HttpRequest request) {
        Request.Builder builder = new Request.Builder()
                .url(request.getFullUrl());

        // 添加headers
        request.getHeaders().forEach(builder::addHeader);

        // 添加Content-Type（如果没有手动设置）
        if (!request.getHeaders().containsKey("Content-Type") && request.getContentType() != null) {
            builder.addHeader("Content-Type", request.getContentType());
        }

        // 构建请求体
        RequestBody requestBody = buildRequestBody(request);

        // 设置方法和请求体
        builder.method(request.getMethod(), requestBody);

        return builder.build();
    }

    private RequestBody buildRequestBody(HttpRequest request) {
        if (request.getBody() == null) {
            // GET、DELETE等可能没有请求体
            if ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod())) {
                return null;
            }
            return RequestBody.create("", null);
        }

        MediaType mediaType = MediaType.parse(request.getContentType());

        // 根据Content-Type处理不同类型的body
        if (request.getContentType().contains("application/json")) {
            String json;
            if (request.getBody() instanceof String) {
                json = (String) request.getBody();
            } else {
                json = JSON.toJSONString(request.getBody());
            }
            return RequestBody.create(json, mediaType);
        } else if (request.getContentType().contains("application/x-www-form-urlencoded")) {
            FormBody.Builder formBuilder = new FormBody.Builder();
            if (request.getBody() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> formData = (Map<String, String>) request.getBody();
                formData.forEach(formBuilder::add);
            }
            return formBuilder.build();
        } else if (request.getBody() instanceof String) {
            return RequestBody.create((String) request.getBody(), mediaType);
        } else {
            // 其他类型转JSON
            return RequestBody.create(JSON.toJSONString(request.getBody()), mediaType);
        }
    }

    private HttpResponse buildHttpResponse(Response response) throws IOException {
        int statusCode = response.code();
        String body = response.body() != null ? response.body().string() : "";

        // 转换headers
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.headers().toMultimap().forEach(headers::put);

        boolean successful = response.isSuccessful();

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
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient.connectionPool().evictAll();
        }
    }
}
