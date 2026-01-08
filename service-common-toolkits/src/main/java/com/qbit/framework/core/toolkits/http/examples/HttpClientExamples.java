package com.qbit.framework.core.toolkits.http.examples;

import com.qbit.framework.core.api.model.web.Result;
import com.qbit.framework.core.toolkits.http.HttpClient;
import com.qbit.framework.core.toolkits.http.HttpRequest;
import com.qbit.framework.core.toolkits.http.HttpResponse;
import com.qbit.framework.core.toolkits.http.HttpUtils;
import com.qbit.framework.core.toolkits.http.interceptor.LoggingInterceptor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * HTTP 工具类使用示例
 *
 * @author zhoubobing
 * @date 2026/1/8
 */
public class HttpClientExamples {

    /**
     * 示例1：简单的 GET 请求（自动追踪）
     */
    public void example1_simpleGet() {
        // 最简单的用法，自动传播 traceId 和 spanId
        HttpResponse response = HttpUtils.get("https://api.example.com/users");
        System.out.println(response.getBody());
    }

    /**
     * 示例2：POST 请求并解析响应
     */
    public void example2_postWithParse() {
        User user = new User("张三", 25);

        // 发送请求并直接解析为对象
        User createdUser = HttpUtils.post("https://api.example.com/users", user)
                .as(User.class);

        System.out.println("Created user: " + createdUser);
    }

    /**
     * 示例2-2：使用统一响应格式（实际业务最常用）
     */
    public void example2_2_withUnifiedResponse() {
        User user = new User("张三", 25);

        // 解析为统一响应格式 Result<User>
        Result<User> result = HttpUtils.post("https://api.example.com/users", user)
                .asGeneric(Result.class, User.class);

        if (result.isSuccess()) {
            User createdUser = result.getData();
            System.out.println("Created user: " + createdUser);
        } else {
            System.err.println("Create failed: " + result.getMessage());
        }
    }

    /**
     * 示例3：使用构建器模式
     */
    public void example3_builderPattern() {
        HttpRequest request = HttpUtils.request("https://api.example.com/users")
                .post()
                .header("Authorization", "Bearer token123")
                .header("X-Request-Id", "req-001")
                .queryParam("page", "1")
                .queryParam("size", "10")
                .jsonBody(Map.of("name", "李四", "age", 30))
                .timeout(Duration.ofSeconds(5))
                .build();

        HttpResponse response = HttpUtils.getDefaultClient().execute(request);
        List<User> users = response.asList(User.class);
    }

    /**
     * 示例4：创建带链路追踪的自定义客户端
     */
    public void example4_customClientWithTrace() {
        // 使用构建器创建自定义客户端
        HttpClient client = HttpUtils.builder()
                .useOkHttp()                                    // 使用 OkHttp
                .enableTrace(true)                              // 启用链路追踪
                .timeout(Duration.ofSeconds(10))                // 设置超时
                .addInterceptor(new LoggingInterceptor(true, false))  // 添加日志拦截器
                .build();

        HttpRequest request = HttpUtils.request("https://api.example.com/orders")
                .get()
                .build();

        HttpResponse response = client.execute(request);
        System.out.println("Status: " + response.getStatusCode());
    }

    /**
     * 示例5：不使用链路追踪
     */
    public void example5_withoutTrace() {
        // 获取不带追踪的客户端
        HttpClient client = HttpUtils.getClientWithoutTrace();

        HttpRequest request = HttpUtils.request("https://api.example.com/public/data")
                .get()
                .build();

        HttpResponse response = client.execute(request);
        Map<String, Object> data = response.asMap();
    }

    /**
     * 示例6：实际业务场景 - 调用第三方 API（统一响应格式）
     */
    public void example6_thirdPartyApiClient() {
        // 第三方 API 统一响应格式
        class ApiResponse<T> {
            private int code;
            private String message;
            private T data;

            public boolean isSuccess() {
                return code == 0;
            }

            public T getData() {
                return data;
            }

            public String getMessage() {
                return message;
            }
        }

        // 创建专用客户端（复用）
        HttpClient apiClient = HttpUtils.builder()
                .enableTrace(true)
                .timeout(Duration.ofSeconds(30))
                .build();

        // 调用 API - 创建订单
        Map<String, String> headers = Map.of(
                "X-API-Key", "your-api-key",
                "X-Client-Version", "1.0.0"
        );

        ApiResponse<Order> response = HttpUtils.post(
                "https://api.partner.com/v1/orders",
                headers,
                Map.of("orderId", "12345", "amount", 100)
        ).asGeneric(ApiResponse.class, Order.class);

        if (response.isSuccess()) {
            Order order = response.getData();
            System.out.println("Order created: " + order);
        } else {
            System.err.println("Request failed: " + response.getMessage());
        }

        // 查询订单列表
        ApiResponse<List<Order>> listResponse = HttpUtils.get(
                "https://api.partner.com/v1/orders",
                headers
        ).asGeneric(ApiResponse.class, List.class, Order.class);

        if (listResponse.isSuccess()) {
            List<Order> orders = listResponse.getData();
            System.out.println("Total orders: " + orders.size());
        }
    }

    /**
     * 示例7：微服务间调用（自动追踪传播）
     */
    public void example7_microserviceCall() {
        // 默认客户端自动传播 traceId 和 spanId
        String orderServiceUrl = "http://order-service/api/orders";

        // 当前线程有 traceId=abc123, spanId=def456
        // 请求会自动添加：
        // X-Trace-Id: abc123
        // X-Span-Id: <新生成的 spanId>

        Order order = HttpUtils.get(orderServiceUrl + "/12345")
                .as(Order.class);

        System.out.println("Order: " + order);
    }

    /**
     * 示例8：带重试的请求
     */
    public void example8_withRetry() {
        HttpClient client = HttpUtils.builder()
                .enableTrace(true)
                .addInterceptor(new RetryInterceptor(3))  // 自定义重试拦截器
                .build();

        HttpRequest request = HttpUtils.request("https://api.example.com/data")
                .get()
                .build();

        // 统一响应格式
        class Result<T> {
            private Integer code;
            private String message;
            private T data;

            public boolean isSuccess() {
                return code != null && code == 200;
            }

            public T getData() {
                return data;
            }

            public String getMessage() {
                return message;
            }
        }

        HttpResponse response = client.execute(request);
    }

    /**
     * 示例9：安全解析（不抛异常）
     */
    public void example9_safeParse() {
        HttpResponse response = HttpUtils.get("https://api.example.com/user/999");

        // 方式1：失败返回 null
        User user = response.asOrNull(User.class);
        if (user == null) {
            System.out.println("User not found or parse failed");
        }

        // 方式2：失败返回默认值
        User defaultUser = response.asOrDefault(User.class, new User("Unknown", 0));
    }

    // ========== 辅助类 ==========

    static class User {
        private String name;
        private int age;

        public User() {
        }

        public User(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString() {
            return "User{name='" + name + "', age=" + age + '}';
        }
    }

    static class Order {
        private String orderId;
        private int amount;

        @Override
        public String toString() {
            return "Order{orderId='" + orderId + "', amount=" + amount + '}';
        }
    }

    /**
     * 自定义签名拦截器示例
     */
    static class ApiSignatureInterceptor implements com.qbit.framework.core.toolkits.http.interceptor.HttpClientInterceptor {
        @Override
        public HttpRequest intercept(HttpRequest request) {
            // 计算签名
            String sign = calculateSignature(request);

            // 添加签名到请求头
            return HttpRequest.builder(request.getUrl())
                    .method(request.getMethod())
                    .headers(request.getHeaders())
                    .queryParams(request.getQueryParams())
                    .body(request.getBody())
                    .contentType(request.getContentType())
                    .connectTimeout(request.getConnectTimeout())
                    .readTimeout(request.getReadTimeout())
                    .writeTimeout(request.getWriteTimeout())
                    .followRedirects(request.isFollowRedirects())
                    .header("X-Signature", sign)
                    .build();
        }

        private String calculateSignature(HttpRequest request) {
            // 实现签名逻辑
            return "signature";
        }

        @Override
        public int getOrder() {
            return -50;  // 在追踪之后，日志之前
        }
    }

    /**
     * 自定义重试拦截器示例（注意：这只是示例，实际重试需要在客户端层面实现）
     */
    static class RetryInterceptor implements com.qbit.framework.core.toolkits.http.interceptor.HttpClientInterceptor {
        private final int maxRetries;

        public RetryInterceptor(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public HttpRequest intercept(HttpRequest request) {
            // 拦截器只能修改请求，不能修改执行逻辑
            // 真正的重试需要在外层实现
            return request;
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }
}
