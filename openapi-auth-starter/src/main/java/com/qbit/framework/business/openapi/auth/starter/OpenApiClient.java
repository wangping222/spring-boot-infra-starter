package com.qbit.framework.business.openapi.auth.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.qbit.framework.business.openapi.auth.starter.config.JacksonConfig;
import com.qbit.framework.business.openapi.auth.starter.model.AccessTokenResponse;
import com.qbit.framework.business.openapi.auth.starter.model.ApiPathEnum;
import com.qbit.framework.business.openapi.auth.starter.model.ApiResponse;
import com.qbit.framework.business.openapi.auth.starter.model.GetCodeResponse;
import com.qbit.framework.business.openapi.auth.starter.properties.OpenapiProperties;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenApiClient {

    private final OpenapiProperties properties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenApiClient(OpenapiProperties properties) {
        this(properties, new OkHttpClient());
    }

    public OpenApiClient(OpenapiProperties properties, OkHttpClient httpClient) {
        this.properties = Objects.requireNonNull(properties, "OpenapiProperties must not be null");
        this.httpClient = httpClient == null ? new OkHttpClient() : httpClient;
        this.objectMapper = JacksonConfig.defaultMapper();
    }

    /**
     * 调用 Open API 授权接口：GET /open-api/v3/oauth/authorize
     * 使用配置中的 clientId 和 baseUrl。
     *
     * @return ApiResponse<GetCodeResponse>
     */
    public ApiResponse<GetCodeResponse> getCode() {
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
    public ApiResponse<AccessTokenResponse> generateAccessToken(String code) {
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
                .addHeader("Accept", "application/json");
        if ("POST".equalsIgnoreCase(method)) {
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonBody == null ? "{}" : jsonBody);
            builder.post(requestBody).addHeader("Content-Type", "application/json");
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

        public OpenApiClient build() {
            String baseUrl = properties.getBaseUrl();
            String clientId = properties.getClientId();

            if (baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalStateException("OpenapiProperties.baseUrl 未配置");
            }
            if (clientId == null || clientId.isBlank()) {
                throw new IllegalStateException("OpenapiProperties.clientId 未配置");
            }
            return new OpenApiClient(properties, httpClient);
        }
    }

    public static void main(String[] args) {


        OpenApiClient client = OpenApiClient.builder()
                .baseUrl("https://api-sandbox.interlace.money")
                .clientId("qbitbbcbd8dd72254101aaaa")
                .build();

        ApiResponse<GetCodeResponse> authorize = client.getCode();
        ApiResponse<AccessTokenResponse> accessTokenResponseApiResponse = client.generateAccessToken(authorize.getData().getCode());

        System.out.println(authorize);
        System.out.println(accessTokenResponseApiResponse);

    }
}