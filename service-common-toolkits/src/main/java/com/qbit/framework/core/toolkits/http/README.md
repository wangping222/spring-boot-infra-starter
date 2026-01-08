# HTTP 工具类使用指南

## 简介

本 HTTP 工具类提供了灵活易用、支持扩展的 HTTP 请求功能，支持 JDK HttpClient 和 OkHttp 两种实现。

**核心特性：**
- ✅ 自动链路追踪传播（默认启用）
- ✅ 灵活的拦截器机制
- ✅ 简洁的 API 设计
- ✅ 支持多种 HTTP 客户端实现

## 核心类说明

- **HttpUtils** - 静态工具类，提供最简便的使用方式
- **HttpRequest** - 请求对象，使用建造者模式构建
- **HttpResponse** - 响应对象，封装响应信息
- **HttpClient** - 客户端接口，支持不同实现
- **JdkHttpClientImpl** - JDK 11+ HttpClient 实现（默认）
- **OkHttpClientImpl** - OkHttp 实现
- **TraceInterceptor** - 链路追踪拦截器（默认启用）
- **HttpClientInterceptor** - 拦截器接口，支持自定义扩展

## 快速开始

### 1. 简单的 GET 请求

```java
// 最简单的用法
HttpResponse response = HttpUtils.get("https://api.example.com/users");
System.out.println(response.getBody());
```

### 2. POST JSON 请求

```java
// 发送JSON数据
User user = new User("张三", 25);
HttpResponse response = HttpUtils.post("https://api.example.com/users", user);

// 或使用 Map
Map<String, Object> data = Map.of("name", "张三", "age", 25);
HttpResponse response = HttpUtils.post("https://api.example.com/users", data);
```

### 3. 带请求头的请求

```java
Map<String, String> headers = new HashMap<>();
headers.put("Authorization", "Bearer your-token");
headers.put("X-Request-Id", UUID.randomUUID().toString());

HttpResponse response = HttpUtils.get("https://api.example.com/profile", headers);
```

### 4. 带查询参数的 GET 请求

```java
Map<String, String> params = Map.of("page", "1", "size", "10", "sort", "name");
HttpResponse response = HttpUtils.get("https://api.example.com/users", null, params);
```

## 高级用法

### 使用构建器模式

```java
HttpRequest request = HttpUtils.request("https://api.example.com/users")
    .post()
    .header("Authorization", "Bearer token")
    .header("Content-Type", "application/json")
    .queryParam("version", "v1")
    .jsonBody(user)
    .timeout(Duration.ofSeconds(5))
    .build();

HttpResponse response = HttpUtils.getDefaultClient().execute(request);
```

### 更多构建器示例

```java
// PUT 请求
HttpRequest request = HttpUtils.request("https://api.example.com/users/1")
    .put()
    .header("Authorization", "Bearer token")
    .jsonBody(updatedUser)
    .build();

// DELETE 请求
HttpRequest request = HttpUtils.request("https://api.example.com/users/1")
    .delete()
    .header("Authorization", "Bearer token")
    .build();

// 表单提交
Map<String, String> formData = Map.of("username", "admin", "password", "123456");
HttpRequest request = HttpUtils.request("https://api.example.com/login")
    .post()
    .formBody(formData)
    .build();

// 自定义超时
HttpRequest request = HttpUtils.request("https://api.example.com/slow-api")
    .get()
    .connectTimeout(Duration.ofSeconds(5))
    .readTimeout(Duration.ofSeconds(60))
    .build();
```

## 响应处理

### 1. 检查响应状态

```java
HttpResponse response = HttpUtils.get("https://api.example.com/users");

if (response.isSuccessful()) {
    System.out.println("请求成功: " + response.getBody());
} else {
    System.err.println("请求失败，状态码: " + response.getStatusCode());
}
```

### 2. 解析 JSON 响应

```java
// 方式1：直接从 response 解析（推荐）
HttpResponse response = HttpUtils.get("https://api.example.com/user/1");
User user = response.as(User.class);

// 解析为 List
HttpResponse response = HttpUtils.get("https://api.example.com/users");
List<User> users = response.asList(User.class);

// 解析为 Map<String, Object>
Map<String, Object> config = response.asMap();

// 解析为 Map<String, User>
Map<String, User> userMap = response.asMapOf(User.class);

// 解析为 List<Map<String, Object>>
List<Map<String, Object>> items = response.asListOfMap();

// 方式2：使用工具类方法（向后兼容）
User user = HttpUtils.parseResponse(response, User.class);
List<User> users = HttpUtils.parseResponse(response, new TypeReference<List<User>>() {});
```

**常用解析方法：**

```java
// 1. 解析为简单对象
User user = response.as(User.class);

// 2. 解析为 List
List<User> users = response.asList(User.class);

// 3. 解析为统一响应包装类（最常用⭐）
Result<User> result = response.asGeneric(Result.class, User.class);
Result<List<User>> result = response.asGeneric(Result.class, List.class, User.class);
ApiResponse<Order> apiResp = response.asGeneric(ApiResponse.class, Order.class);

// 4. 解析为 Map
Map<String, Object> map = response.asMap();
Map<String, User> userMap = response.asMapOf(User.class);
```

**统一响应格式示例：**

```java
// 假设后端统一返回格式：
class Result<T> {
    private int code;
    private String message;
    private T data;
}

// 使用方式：
Result<User> result = HttpUtils.get("https://api.example.com/user/1")
    .asGeneric(Result.class, User.class);

if (result.getCode() == 200) {
    User user = result.getData();
}

// List 数据：
Result<List<User>> result = HttpUtils.get("https://api.example.com/users")
    .asGeneric(Result.class, List.class, User.class);

List<User> users = result.getData();
```

**安全解析方法：**

```java
// 解析失败返回 null
User user = response.asOrNull(User.class);

// 解析失败返回默认值
User user = response.asOrDefault(User.class, new User());
```

### 3. 获取响应头

```java
HttpResponse response = HttpUtils.get("https://api.example.com/users");

// 获取单个 header
String contentType = response.getHeader("Content-Type");

// 获取所有同名 header
List<String> setCookies = response.getHeaders("Set-Cookie");
```

## 使用不同的 HTTP 客户端

### 1. 使用 JDK HttpClient（默认，带链路追踪）

```java
// 方式1：使用默认客户端（自动追踪）
HttpResponse response = HttpUtils.get("https://api.example.com/users");

// 方式2：显式创建（带追踪）
HttpClient client = HttpUtils.createJdkHttpClient();
HttpRequest request = HttpUtils.request("https://api.example.com/users").get().build();
HttpResponse response = client.execute(request);

// 方式3：创建不带追踪的客户端
HttpClient client = HttpUtils.createJdkHttpClient(false);
```

### 2. 使用 OkHttp

```java
// 方式1：使用默认配置（带追踪）
HttpClient client = HttpUtils.createOkHttpClient();
HttpRequest request = HttpUtils.request("https://api.example.com/users").get().build();
HttpResponse response = client.execute(request);

// 方式2：自定义配置
OkHttpClient okHttpClient = new OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(new LoggingInterceptor())
    .build();

HttpClient client = new OkHttpClientImpl(okHttpClient)
    .addInterceptor(new TraceInterceptor());  // 手动添加追踪
HttpResponse response = client.execute(request);
```

### 3. 带超时的客户端

```java
// 带追踪
HttpClient client = HttpUtils.createClientWithTimeout(Duration.ofSeconds(5));

// 不带追踪
HttpClient client = HttpUtils.createClientWithTimeout(Duration.ofSeconds(5), false);
```

### 4. 使用构建器创建客户端

```java
// 完全自定义的客户端
HttpClient client = HttpUtils.builder()
    .useOkHttp()                                    // 使用 OkHttp
    .enableTrace(true)                              // 启用追踪
    .timeout(Duration.ofSeconds(10))                // 设置超时
    .addInterceptor(new LoggingInterceptor())       // 添加日志拦截器
    .build();
```

## 链路追踪

### 自动追踪传播

默认情况下，所有通过 `HttpUtils` 发起的请求都会自动传播链路追踪信息（traceId 和 spanId）。

```java
// 自动传播当前线程的 traceId 和 spanId
HttpResponse response = HttpUtils.get("https://api.example.com/users");

// 响应头会自动包含：
// X-Trace-Id: <当前的 traceId>
// X-Span-Id: <新生成的 spanId>
```

### 禁用链路追踪

如果某些场景不需要追踪：

```java
// 方式1：使用不带追踪的默认客户端
HttpClient client = HttpUtils.getClientWithoutTrace();
HttpResponse response = client.execute(request);

// 方式2：创建时指定
HttpClient client = HttpUtils.createJdkHttpClient(false);

// 方式3：使用构建器
HttpClient client = HttpUtils.builder()
    .enableTrace(false)
    .build();
```

### 自定义追踪行为

```java
// 创建自定义追踪拦截器
TraceInterceptor traceInterceptor = new TraceInterceptor(
    true,   // propagateTrace: 是否传播追踪信息
    false   // generateNewSpan: 是否生成新的 spanId（false=使用当前 spanId）
);

HttpClient client = new JdkHttpClientImpl()
    .addInterceptor(traceInterceptor);
```

### 链路追踪原理

当发起 HTTP 请求时：

1. `TraceInterceptor` 从当前线程的 MDC 中获取 `traceId` 和 `spanId`
2. 将 `traceId` 添加到请求头 `X-Trace-Id`
3. 为下游服务生成新的 `spanId`，添加到请求头 `X-Span-Id`
4. 下游服务收到请求后，继续传播追踪信息

这样就形成了完整的调用链路：

```
服务A (traceId=123, spanId=456)
  └─> HTTP请求 (X-Trace-Id=123, X-Span-Id=789)
      └─> 服务B (traceId=123, spanId=789)
          └─> HTTP请求 (X-Trace-Id=123, X-Span-Id=abc)
              └─> 服务C (traceId=123, spanId=abc)
```

## 自定义拦截器

### 实现拦截器接口

```java
public class CustomInterceptor implements HttpClientInterceptor {
    
    @Override
    public HttpRequest intercept(HttpRequest request) {
        // 修改请求，例如添加签名
        String sign = calculateSign(request);
        
        return HttpUtils.request(request.getUrl())
            .method(request.getMethod())
            .headers(request.getHeaders())
            .header("X-Sign", sign)
            .body(request.getBody())
            // ... 其他属性
            .build();
    }
    
    @Override
    public int getOrder() {
        return 0;  // 优先级，数字越小越先执行
    }
}
```

### 使用自定义拦截器

```java
// 方式1：添加到客户端
HttpClient client = HttpUtils.createJdkHttpClient()
    .addInterceptor(new CustomInterceptor())
    .addInterceptor(new LoggingInterceptor());

// 方式2：使用构建器
HttpClient client = HttpUtils.builder()
    .addInterceptor(new CustomInterceptor())
    .addInterceptor(new LoggingInterceptor())
    .build();
```

### 内置拦截器

**TraceInterceptor** - 链路追踪拦截器
```java
HttpClient client = new JdkHttpClientImpl()
    .addInterceptor(new TraceInterceptor());
```

**LoggingInterceptor** - 日志拦截器
```java
HttpClient client = new JdkHttpClientImpl()
    .addInterceptor(new LoggingInterceptor(true, true));  // 记录 headers 和 body
```

## 实际应用场景

### 场景1：调用第三方 API（统一响应格式）

```java
public class ExternalApiClient {
    private static final String API_BASE_URL = "https://api.example.com";
    private static final String API_KEY = "your-api-key";
    
    // 第三方 API 统一响应格式
    static class ApiResult<T> {
        private int code;
        private String message;
        private T data;
        
        public T getData() { return data; }
        public boolean isSuccess() { return code == 0; }
    }
    
    public List<User> getUsers(int page, int size) {
        Map<String, String> headers = Map.of("X-API-Key", API_KEY);
        Map<String, String> params = Map.of(
            "page", String.valueOf(page), 
            "size", String.valueOf(size)
        );
        
        // 解析为统一响应格式
        ApiResult<List<User>> result = HttpUtils.get(API_BASE_URL + "/users", headers, params)
            .asGeneric(ApiResult.class, List.class, User.class);
        
        if (res（内部统一格式）

```java
public class OrderServiceClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    
    // 微服务统一响应格式
    static class ServiceResult<T> {
        private Integer code;
        private String msg;
        private T data;
        
        public T getData() { return data; }
        public boolean isOk() { return code != null && code == 200; }
    }
    
    public OrderServiceClient(String baseUrl) {
        this.baseUrl = baseUrl;
        // 使用自定义超时配置（带链路追踪）
        this.httpClient = HttpUtils.createClientWithTimeout(Duration.ofSeconds(3));
    }
    
    public Order getOrder(String orderId) {
        HttpRequest request = HttpUtils.request(baseUrl + "/orders/" + orderId)
            .get()
            .header("X-Service", "user-service")
            .build();
        
        ServiceResult<Order> result = httpClient.execute(request)
            .asGeneric(ServiceResult.class, Order.class);
        
        return result.getData();
    }
    
    public List<Order> getOrdersByUser(String userId) {
        HttpRequest request = HttpUtils.request(baseUrl + "/orders")
            .get()
            .queryParam("userId", userId)
            .header("X-Service", "user-service")
            .build();
        
        ServiceResult<List<Order>> result = httpClient.execute(request)
            .asGeneric(ServiceResult.class, List.class, Order.class);
        
        if (result.isOk()) {
            return result.getData();
        }
        throw new RuntimeException("查询订单失败: " + result.msg);
    }
    
    public Order createOrder(CreateOrderRequest req) {
        HttpRequest request = HttpUtils.request(baseUrl + "/orders")
            .post()
            .jsonBody(req)
            .build();
        
        ServiceResult<Order> result = httpClient.execute(request)
            .asGeneric(ServiceResult.class, Order.class);
        
        return result.getData(
        this.baseUrl = baseUrl;
        // 使用自定义超时配置
        this.httpClient = HttpUtils.createClientWithTimeout(Duration.ofSeconds(3));
    }
    
    public Order getOrder(String orderId) {
        HttpRequest request = HttpUtils.request(baseUrl + "/orders/" + orderId)
            .geresponse.as(Order.class);  // 直接解析为对象
    }
    
    public List<Order> getOrdersByUser(String userId) {
        HttpRequest request = HttpUtils.request(baseUrl + "/orders")
            .get()
            .queryParam("userId", userId)
            .header("X-Service", "user-service")
            .build();
        
        HttpResponse response = httpClient.execute(request);
        return response.asList(Order.class);  // 直接解析为 List
            .header("X-Service", "user-service")
            .build();
        
        HttpResponse response = httpClient.execute(request);
        return HttpUtils.parseResponse(response, Order.class);
    }
}
```

### 场景3：带重试机制的请求

```java
public class RetryableHttpClient {
    private final HttpClient httpClient = HttpUtils.createJdkHttpClient();
    private final int maxRetries = 3;
    
    public HttpResponse executeWithRetry(HttpRequest request) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < maxRetries) {
            try {
                HttpResponse response = httpClient.execute(request);
                if (response.isSuccessful()) {
                    return response;
                }
                // 如果是客户端错误（4xx），不重试
                if (response.getStatusCode() >= 400 && response.getStatusCode() < 500) {
                    return response;
                }
            } catch (HttpClientException e) {
                lastException = e;
            }
            
            attempt++;
            if (attempt < maxRetries) {
                try {
                    Thread.sleep(1000L * attempt); // 指数退避
                } catch (InterruptedException e) {
   **默认启用链路追踪** - 所有通过 `HttpUtils` 的请求都会自动传播 traceId 和 spanId
2. 如需更强大的功能（如连接池管理、OkHttp 拦截器等），建议使用 OkHttp
3. 生产环境建议复用 HttpClient 实例，避免频繁创建
4. 设置合理的超时时间，避免请求长时间阻塞
5. 对敏感信息（如 token）要注意日志脱敏
6. 拦截器按优先级（Order）执行，数字越小越先执行
7. 链路追踪依赖 SLF4J MDC，确保日志框架正确配置
        
        throw new HttpClientException("Request failed after " + maxRetries + " retries", lastException);
    }
}
```

## 扩展自定义实现

### 实现自定义 HttpClient

```java
public class CustomHttpClient implements HttpClient {
    
    @Override
    public HttpResponse execute(HttpRequest request) throws HttpClientException {
        // 1. 实现你的 HTTP 请求逻辑
        // 2. 返回 HttpResponse 对象
        
        // 示例：使用 Apache HttpClient
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpUriRequest httpRequest = buildApacheRequest(request);
            
            try (CloseableHttpResponse response = client.execute(httpRequest)) {
                return buildHttpResponse(response);
            }
        } catch (IOException e) {
            throw new HttpClientException("Request failed", e);
        }
    }
    
    @Override
    public void close() {
        // 释放资源
    }
    
    private HttpUriRequest buildApacheRequest(HttpRequest request) {
        // 转换请求对象
        return null;
    }
    
    private HttpResponse buildHttpResponse(CloseableHttpResponse response) {
        // 转换响应对象
        return null;
    }
}
```

## 最佳实践

### 1. 使用连接池

对于频繁调用的场景，复用 HttpClient 实例：

```java
@Component
public class ApiClient {
    private final HttpClient httpClient;
    
    public ApiClient() {
        // 创建一次，复用多次
        this.httpClient = HttpUtils.createOkHttpClient();
    }
    
    public HttpResponse call(String url) {
        HttpRequest request = HttpUtils.request(url).get().build();
        return httpClient.execute(request);
    }
}
```

### 2. 设置合理的超时时间

// 方式1：直接解析（自动检查成功状态）
try {
    HttpResponse response = HttpUtils.get(url);
    User user = response.as(User.class);  // 失败自动抛出异常
    return user;
} catch (HttpClientException e) {
    log.error("HTTP请求异常", e);
    throw new BusinessException("网络请求失败", e);
}

// 方式2：手动检查状态
try {
    HttpResponse response = HttpUtils.get(url);
    if (response.isSuccessful()) {
        return response.as(User.class);
    } else {
        log.error("API调用失败，状态码: {}, 响应: {}", 
            response.getStatusCode(), response.getBody());
        throw new BusinessException("API调用失败");
    }
} catch (HttpClientException e) {
    log.error("HTTP请求异常", e);
    throw new BusinessException("网络请求失败", e);
}

// 方式3：安全解析（不抛异常）
User user = HttpUtils.get(url).asOrNull(User.class);
if (user == null) {
    // 处理失败情况
    HttpResponse response = HttpUtils.get(url);
    if (response.isSuccessful()) {
        return HttpUtils.parseResponse(response, User.class);
    } else {
        log.error("API调用失败，状态码: {}, 响应: {}", 
            response.getStatusCode(), response.getBody());
        throw new BusinessException("API调用失败");
    }
} catch (HttpClientException e) {
    log.error("HTTP请求异常", e);
    throw new BusinessException("网络请求失败", e);
}
```

## 依赖说明

### JDK HttpClient
- 要求 JDK 11+
- 无需额外依赖

### OkHttp
需要在 pom.xml 添加依赖：

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

## 注意事项

1. 默认使用 JDK HttpClient，性能良好且无需额外依赖
2. 如需更强大的功能（如连接池管理、拦截器等），建议使用 OkHttp
3. 生产环境建议复用 HttpClient 实例，避免频繁创建
4. 设置合理的超时时间，避免请求长时间阻塞
5. 对敏感信息（如 token）要注意日志脱敏
