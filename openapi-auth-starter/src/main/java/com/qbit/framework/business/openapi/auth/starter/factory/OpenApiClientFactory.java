package com.qbit.framework.business.openapi.auth.starter.factory;

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
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class OpenApiClientFactory {
    private final static String SUCCESS_CODE = "000000";


    private final OpenapiProperties properties;
    private final OkHttpClient httpClient;
    private final RedisTemplate<String, String> redisTemplate;


    public OpenApiClientFactory(OpenapiProperties properties, OkHttpClient httpClient, RedisTemplate<String, String> redisTemplate) {
        this.properties = Objects.requireNonNull(properties, "OpenapiProperties must not be null");
        this.httpClient = httpClient == null ? buildHttpClient(properties) : httpClient;
        this.redisTemplate = redisTemplate;
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
        // 先尝试从 Redis 缓存中读取
        if (redisTemplate != null) {
            String cachedToken = redisTemplate.opsForValue().get(cacheKey);
            if (cachedToken != null && !cachedToken.isBlank()) {
                return cachedToken;
            }
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
        // 过期秒数
        Integer expiresIn = tokenResp.getData().getExpiresIn();
        // 10 分钟安全缓冲，避免边界过期
        long ttlSeconds = Math.max(1, (long) expiresIn - 600);
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(cacheKey, token, Duration.ofSeconds(ttlSeconds));
        }
        return token;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private OpenapiProperties properties = new OpenapiProperties();
        private OkHttpClient httpClient;
        private RedisTemplate<String, String> redisTemplate;

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

        public Builder redisTemplate(RedisTemplate<String, String> redisTemplate) {
            this.redisTemplate = redisTemplate;
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
            return new OpenApiClientFactory(properties, httpClient, redisTemplate);
        }
    }

    public ApiClient getApiClient() {
        ApiClient apiClient = buildApiClient(properties, httpClient);
        List<String> names = List.of("ApiKeyAuth", "authHeader");
        String validAccessToken = this.getValidAccessToken();
        for (String name : names) {
            ApiKeyAuth authHeader = (ApiKeyAuth) apiClient.getAuthentication(name);
            authHeader.setApiKey(validAccessToken);
        }
        return apiClient;
    }
}