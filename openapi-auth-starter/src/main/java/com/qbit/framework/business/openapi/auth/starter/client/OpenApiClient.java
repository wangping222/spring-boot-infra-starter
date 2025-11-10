package com.qbit.framework.business.openapi.auth.starter.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.qbit.framework.business.openapi.auth.starter.config.JacksonConfig;
import com.qbit.framework.business.openapi.auth.starter.model.ApiPathEnum;
import com.qbit.framework.business.openapi.auth.starter.model.ApiResponse;
import com.qbit.framework.business.openapi.auth.starter.model.GetCodeResponse;
import com.qbit.framework.business.openapi.auth.starter.properties.OpenapiProperties;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
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
        HttpUrl url = buildAuthorizeUrl();

        try {
            String body = sendGet(url);
            if (body == null || body.isBlank()) {
                ApiResponse<GetCodeResponse> empty = new ApiResponse<>();
                empty.setCode("-1");
                empty.setMessage("Empty response body");
                return empty;
            }
            return readApiResponse(body, GetCodeResponse.class);
        } catch (IOException e) {
            ApiResponse<GetCodeResponse> error = new ApiResponse<>();
            error.setCode("-1");
            error.setMessage("HTTP request or parse failed: " + e.getMessage());
            return error;
        }
    }

    // 使用 Jackson 泛型反序列化，将 body 解析为 ApiResponse<T>
    private <T> ApiResponse<T> readApiResponse(String body, Class<T> clazz) throws IOException {
        TypeFactory tf = objectMapper.getTypeFactory();
        return objectMapper.readValue(body, tf.constructParametricType(ApiResponse.class, clazz));
    }

    // 业务参数构建：组装授权接口的完整 URL
    private HttpUrl buildAuthorizeUrl() {
        String baseUrl = properties.getBaseUrl();
        String clientId = properties.getClientId();
        ApiPathEnum getCodeApi = ApiPathEnum.GET_CODE;

        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        HttpUrl base = HttpUrl.parse(normalizedBase);
        if (base == null) {
            throw new IllegalArgumentException("非法的 baseUrl: " + normalizedBase);
        }

        HttpUrl.Builder urlBuilder = base.newBuilder()
                .addPathSegments(getCodeApi.getPath())
                .addQueryParameter("clientId", clientId);

        return urlBuilder.build();
    }

    // 请求发送：以 GET 方法请求指定 URL，并返回响应体字符串
    private String sendGet(HttpUrl url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody responseBody = response.body();
            return responseBody == null ? null : responseBody.string();
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
                .clientId("qbitbbcbd8dd72254101")
                .build();

        ApiResponse<GetCodeResponse> authorize = client.getCode();
        System.out.println(authorize);

    }
}