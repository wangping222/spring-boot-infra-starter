package com.qbit.framework.business.openapi.auth.starter;


import com.qbit.framework.business.openapi.auth.starter.factory.OpenApiClientFactory;
import money.interlace.sdk.api.BudgetsApi;
import money.interlace.sdk.invoker.ApiException;
import money.interlace.sdk.invoker.Configuration;
import money.interlace.sdk.model.BudgetResponse;
import java.lang.reflect.Constructor;
import java.util.function.BiFunction;
import java.util.function.Function;

import java.util.UUID;

public class OpenApiClient {
    private final OpenApiClientFactory factory;

    public OpenApiClient(OpenApiClientFactory factory) {
        this.factory = factory;
    }


    // 通用执行入口（无额外参数）
    public <A, R> R execute(Class<A> apiType, Function<A, R> action) throws ApiException {
        Configuration.setDefaultApiClient(factory.getApiClient());
        A api = newApiInstance(apiType);
        return action.apply(api);
    }

    // 通用执行入口（带一个参数）
    public <A, P, R> R execute(Class<A> apiType, BiFunction<A, P, R> action, P param) throws ApiException {
        Configuration.setDefaultApiClient(factory.getApiClient());
        A api = newApiInstance(apiType);
        return action.apply(api, param);
    }

    private <A> A newApiInstance(Class<A> apiType) {
        try {
            Constructor<A> ctor = apiType.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("无法实例化 API 类型: " + apiType.getName(), e);
        }
    }

    public static void main(String[] args) throws ApiException {
        OpenApiClientFactory factory = OpenApiClientFactory.builder()
                .baseUrl("https://api-sandbox.interlace.money")
                .clientId("qbitbbcbd8dd72254101")
                .build();
        OpenApiClient openApiClient = new OpenApiClient(factory);
        // 示例：调用 BudgetsApi.getBudget
        BudgetResponse res = openApiClient.execute(
                BudgetsApi.class,
                api -> {
                    try {
                        return api.getBudget(UUID.randomUUID(), UUID.randomUUID());
                    } catch (ApiException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        System.out.println(res);
    }
}
