package com.qbit.framework.business.openapi.auth.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qbit.framework.business.openapi.auth.starter.config.JacksonConfig;
import com.qbit.framework.business.openapi.auth.starter.model.AccessTokenResponse;
import com.qbit.framework.business.openapi.auth.starter.model.ApiPathEnum;
import com.qbit.framework.business.openapi.auth.starter.model.ApiResponse;
import com.qbit.framework.business.openapi.auth.starter.model.GetCodeResponse;
import com.qbit.framework.business.openapi.auth.starter.properties.OpenapiProperties;
import money.interlace.sdk.invoker.ApiClient;
import money.interlace.sdk.invoker.auth.ApiKeyAuth;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OpenApiClientFactory {
    private final static String JSON_MEDIA_TYPE = "application/json";
    private final static String SUCCESS_CODE = "000000";


    private final OpenapiProperties properties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Cache<String, AccessTokenHolder> tokenCache;


    public OpenApiClientFactory(OpenapiProperties properties) {
        this(properties, buildHttpClient(properties));
    }

    public OpenApiClientFactory(OpenapiProperties properties, OkHttpClient httpClient) {
        this.properties = Objects.requireNonNull(properties, "OpenapiProperties must not be null");
        this.httpClient = httpClient == null ? buildHttpClient(properties) : httpClient;
        this.objectMapper = JacksonConfig.defaultMapper();
        this.tokenCache = Caffeine.newBuilder().maximumSize(16).build();
    }

    private static ApiClient buildApiClient(OpenapiProperties properties, OkHttpClient httpClient) {
        ApiClient apiClient1 = new ApiClient(httpClient);
        apiClient1.setBasePath(properties.getBaseUrl());
        return apiClient1;
    }

    private static OkHttpClient buildHttpClient(OpenapiProperties properties) {
        return new OkHttpClient.Builder()
                .readTimeout(Objects.isNull(properties.getReadTimeout()) ? 60 : properties.getReadTimeout(), TimeUnit.SECONDS)
                .writeTimeout(Objects.isNull(properties.getWriteTimeout()) ? 60 : properties.getWriteTimeout(), TimeUnit.SECONDS)
                .connectTimeout(Objects.isNull(properties.getConnectionTimeout()) ? 60 : properties.getConnectionTimeout(), TimeUnit.SECONDS)
                .build();
    }

    private String getValidAccessToken() {
        String cacheKey = "access_token:" + properties.getClientId();
        long now = System.currentTimeMillis();
        // 10min安全缓冲，避免边界过期
        long skewMillis = 600_000L;

        AccessTokenHolder holder = tokenCache.getIfPresent(cacheKey);
        if (holder != null && holder.token != null && now < holder.expireAtMillis - skewMillis) {
            return holder.token;
        }

        ApiResponse<GetCodeResponse> codeResp = getCode();
        if (codeResp == null || codeResp.getData() == null || codeResp.getData().getCode() == null || !SUCCESS_CODE.equals(codeResp.getCode())) {
            return null;
        }

        ApiResponse<AccessTokenResponse> tokenResp = generateAccessToken(codeResp.getData().getCode());
        if (tokenResp == null || tokenResp.getData() == null || tokenResp.getData().getAccessToken() == null || !SUCCESS_CODE.equals(tokenResp.getCode())) {
            return null;
        }

        String token = tokenResp.getData().getAccessToken();
        // 秒
        Long expiresIn = tokenResp.getData().getExpiresIn();
        // 默认10分钟
        long ttlMillis = expiresIn != null ? expiresIn * 1000L : 600_000L;
        AccessTokenHolder newHolder = new AccessTokenHolder(token, now + ttlMillis);
        tokenCache.put(cacheKey, newHolder);
        return token;
    }

    private static final class AccessTokenHolder {
        private final String token;
        private final long expireAtMillis;

        private AccessTokenHolder(String token, long expireAtMillis) {
            this.token = token;
            this.expireAtMillis = expireAtMillis;
        }
    }

    /**
     * 调用 Open API 授权接口：GET /open-api/v3/oauth/authorize
     * 使用配置中的 clientId 和 baseUrl。
     *
     * @return ApiResponse<GetCodeResponse>
     */
    private ApiResponse<GetCodeResponse> getCode() {
        HttpUrl url = buildUrl(ApiPathEnum.GET_CODE)
                .addQueryParameter("clientId", properties.getClientId())
                .build();
        return send(url, ApiPathEnum.GET_CODE.getMethod(), null, GetCodeResponse.class);
    }

    /**
     * 调用 Open API 令牌接口：POST /open-api/v3/oauth/access-token
     * 需要传入授权码 code，使用配置中的 clientId 和 baseUrl。
     *
     * @param code 授权码
     * @return ApiResponse<AccessTokenResponse>
     */
    private ApiResponse<AccessTokenResponse> generateAccessToken(String code) {
        if (code == null || code.isBlank()) {
            return buildError("code 不能为空");
        }
        HttpUrl url = buildUrl(ApiPathEnum.GENERATE_ACCESS_TOKEN).build();
        Map<String, Object> payload = new HashMap<>();
        payload.put("clientId", properties.getClientId());
        payload.put("code", code);
        try {
            String json = objectMapper.writeValueAsString(payload);
            return send(url, ApiPathEnum.GENERATE_ACCESS_TOKEN.getMethod(), json, AccessTokenResponse.class);
        } catch (IOException e) {
            return buildError("HTTP request or parse failed: " + e.getMessage());
        }
    }

    // 使用 Jackson 泛型反序列化，将 body 解析为 ApiResponse<T>
    private <T> ApiResponse<T> readApiResponse(String body, Class<T> clazz) throws IOException {
        TypeFactory tf = objectMapper.getTypeFactory();
        return objectMapper.readValue(body, tf.constructParametricType(ApiResponse.class, clazz));
    }

    // 统一：构建基础 URL + 路径
    private HttpUrl.Builder buildUrl(ApiPathEnum api) {
        String baseUrl = properties.getBaseUrl();
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        HttpUrl base = HttpUrl.parse(normalizedBase);
        if (base == null) {
            throw new IllegalArgumentException("非法的 baseUrl: " + normalizedBase);
        }
        return base.newBuilder().addPathSegments(api.getPath());
    }

    // 通用：发送请求并解析
    private <T> ApiResponse<T> send(HttpUrl url, String method, String jsonBody, Class<T> clazz) {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", JSON_MEDIA_TYPE);
        if ("POST".equalsIgnoreCase(method)) {
            RequestBody requestBody = RequestBody.create(jsonBody == null ? "{}" : jsonBody, MediaType.get(JSON_MEDIA_TYPE));
            builder.post(requestBody).addHeader("Content-Type", JSON_MEDIA_TYPE);
        } else {
            builder.get();
        }
        try (Response response = httpClient.newCall(builder.build()).execute()) {
            ResponseBody responseBody = response.body();
            String body = responseBody == null ? null : responseBody.string();
            if (body == null || body.isBlank()) {
                return buildError("Empty response body");
            }
            return readApiResponse(body, clazz);
        } catch (Exception e) {
            return buildError("HTTP request or parse failed: " + e.getMessage());
        }
    }

    // 统一：错误响应构造

    private <T> ApiResponse<T> buildError(String message) {
        ApiResponse<T> error = new ApiResponse<>();
        error.setCode("-1");
        error.setMessage(message);
        return error;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OpenapiProperties properties = new OpenapiProperties();
        private OkHttpClient httpClient;

        public Builder properties(OpenapiProperties properties) {
            this.properties = properties;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.properties.setBaseUrl(baseUrl);
            return this;
        }

        public Builder clientId(String clientId) {
            this.properties.setClientId(clientId);
            return this;
        }

        public Builder okHttpClient(OkHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }

        public OpenApiClientFactory build() {
            String baseUrl = properties.getBaseUrl();
            String clientId = properties.getClientId();

            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalStateException("OpenapiProperties.baseUrl 未配置");
            }
            if (clientId == null || clientId.isBlank()) {
                throw new IllegalStateException("OpenapiProperties.clientId 未配置");
            }
            return new OpenApiClientFactory(properties, httpClient);
        }
    }

    public ApiClient getApiClient() {
        ApiClient apiClient = buildApiClient(properties, httpClient);
        List<String> names = List.of("ApiKeyAuth", "authHeader");
        for (String name : names) {
            ApiKeyAuth authHeader = (ApiKeyAuth) apiClient.getAuthentication(name);
            authHeader.setApiKey(this.getValidAccessToken());
        }
        return apiClient;
    }

    public static void main(String[] args) {

        OpenApiClientFactory factory = OpenApiClientFactory.builder()
                .baseUrl("https://api-sandbox.interlace.money")
                .clientId("qbitbbcbd8dd72254101")
                .build();
        ApiClient apiClient = factory.getApiClient();
        System.out.println(apiClient);
    }
}