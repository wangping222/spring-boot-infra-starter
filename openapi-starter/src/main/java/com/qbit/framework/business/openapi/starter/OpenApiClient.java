package com.qbit.framework.business.openapi.starter;


import com.qbit.framework.business.openapi.starter.factory.OpenApiClientFactory;
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


    /**
     * 通用执行入口（无额外参数）。
     * 在调用前会将默认 ApiClient 设置为由工厂创建的实例。
     *
     * 使用示例：
     * <pre>{@code
     * OpenApiClient client = new OpenApiClient(factory);
     * UUID budgetId = ...;
     * UUID merchantId = ...;
     * BudgetResponse res = client.execute(
     *         BudgetsApi.class,
     *         api -> {
     *             try {
     *                 return api.getBudget(budgetId, merchantId);
     *             } catch (ApiException e) {
     *                 throw new RuntimeException(e);
     *             }
     *         }
     * );
     * }</pre>
     *
     * @param <A> API 类型（例如 {@code BudgetsApi}）
     * @param <R> 返回结果类型
     * @param apiType 要实例化并执行的 API 类型
     * @param action 针对 API 实例的业务操作函数
     * @return 操作返回的结果
     * @throws IllegalStateException 当 API 类型无法实例化时抛出
     */
    public <A, R> R execute(Class<A> apiType, Function<A, R> action) {
        Configuration.setDefaultApiClient(factory.getApiClient());
        A api = newApiInstance(apiType);
        return action.apply(api);
    }

    /**
     * 通用执行入口（带一个参数）。
     * 在调用前会将默认 ApiClient 设置为由工厂创建的实例。
     *
     * 使用示例：
     * <pre>{@code
     * OpenApiClient client = new OpenApiClient(factory);
     * UUID id = ...;
     * SomeResponse res = client.execute(
     *         SomeApi.class,
     *         (api, uuid) -> {
     *             try {
     *                 return api.getById(uuid);
     *             } catch (ApiException e) {
     *                 throw new RuntimeException(e);
     *             }
     *         },
     *         id
     * );
     * }</pre>
     *
     * @param <A> API 类型
     * @param <P> 传入的业务参数类型
     * @param <R> 返回结果类型
     * @param apiType 要实例化并执行的 API 类型
     * @param action 针对 API 实例的业务操作函数，接受一个参数
     * @param param 传递给操作函数的参数
     * @return 操作返回的结果
     * @throws IllegalStateException 当 API 类型无法实例化时抛出
     */
    public <A, P, R> R execute(Class<A> apiType, BiFunction<A, P, R> action, P param) {
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
