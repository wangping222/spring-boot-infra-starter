package com.qbit.framework.business.openapi.auth.starter.factory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qbit.framework.business.openapi.auth.starter.properties.OpenapiProperties;
import lombok.extern.slf4j.Slf4j;
import money.interlace.sdk.api.AuthenticationApi;
import money.interlace.sdk.invoker.ApiClient;
import money.interlace.sdk.invoker.ApiException;
import money.interlace.sdk.invoker.auth.ApiKeyAuth;
import money.interlace.sdk.model.AccessTokenReqDTO;
import money.interlace.sdk.model.AccessTokenRespDTO;
import money.interlace.sdk.model.CodeRespDTO;
import okhttp3.OkHttpClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OpenApiClientFactory {
    private final static String JSON_MEDIA_TYPE = "application/json";
    private final static String SUCCESS_CODE = "000000";


    private final OpenapiProperties properties;
    private final OkHttpClient httpClient;
    private final Cache<String, AccessTokenHolder> tokenCache;


    public OpenApiClientFactory(OpenapiProperties properties) {
        this(properties, buildHttpClient(properties));
    }

    public OpenApiClientFactory(OpenapiProperties properties, OkHttpClient httpClient) {
        this.properties = Objects.requireNonNull(properties, "OpenapiProperties must not be null");
        this.httpClient = httpClient == null ? buildHttpClient(properties) : httpClient;
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
        AuthenticationApi authenticationApi = new AuthenticationApi();

        AccessTokenRespDTO tokenResp = null;
        try {
            CodeRespDTO codeResp = authenticationApi.getCode(properties.getClientId());
            if (codeResp == null || codeResp.getData() == null || !SUCCESS_CODE.equals(codeResp.getCode())) {
                return null;
            }
            AccessTokenReqDTO accessTokenReqDTO = new AccessTokenReqDTO();
            accessTokenReqDTO.setClientId(properties.getClientId());
            accessTokenReqDTO.setCode(codeResp.getData().getCode());

            tokenResp = authenticationApi.getAccessToken(accessTokenReqDTO);
            if (tokenResp == null || tokenResp.getData() == null || !SUCCESS_CODE.equals(tokenResp.getCode())) {
                return null;
            }
        } catch (ApiException e) {
            log.error("get access token error", e);
            return null;
        }

        String token = tokenResp.getData().getAccessToken();
        // 秒
        Integer expiresIn = tokenResp.getData().getExpiresIn();
        // 默认10分钟
        int ttlMillis = expiresIn * 1000;
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

    public static void main(String[] args) throws ApiException {
        AuthenticationApi authenticationApi = new AuthenticationApi();
        CodeRespDTO code = authenticationApi.getCode("qbitbbcbd8dd72254101");
        AccessTokenReqDTO accessTokenReqDTO = new AccessTokenReqDTO();
        accessTokenReqDTO.setClientId("qbitbbcbd8dd72254101");
        accessTokenReqDTO.setCode(code.getData().getCode());

        AccessTokenRespDTO accessToken = authenticationApi.getAccessToken(accessTokenReqDTO);

        OpenApiClientFactory factory = OpenApiClientFactory.builder()
                .baseUrl("https://api-sandbox.interlace.money")
                .clientId("qbitbbcbd8dd72254101")
                .build();
        ApiClient apiClient = factory.getApiClient();
        System.out.println(apiClient);
    }
}