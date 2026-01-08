# HTTP 工具类使用指南

## 简介

本 HTTP 工具类提供了灵活易用、支持扩展的 HTTP 请求功能，支持 JDK HttpClient 和 OkHttp 两种实现。

## 核心类说明

- **HttpUtils** - 静态工具类，提供最简便的使用方式
- **HttpRequest** - 请求对象，使用建造者模式构建
- **HttpResponse** - 响应对象，封装响应信息
- **HttpClient** - 客户端接口，支持不同实现
- **JdkHttpClientImpl** - JDK 11+ HttpClient 实现（默认）
- **OkHttpClientImpl** - OkHttp 实现

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

// 解析为 Map
HttpResponse response = HttpUtils.get("https://api.example.com/config");
Map<String, Object> config = response.asMap();

// 支持泛型的复杂类型
HttpResponse response = HttpUtils.get("https://api.example.com/data");
Map<String, List<User>> data = response.as(new TypeReference<Map<String, List<User>>>() {});

// 方式2：使用工具类方法（向后兼容）
User user = HttpUtils.parseResponse(response, User.class);
List<User> users = HttpUtils.parseResponse(response, new TypeReference<List<User>>() {});
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

### 1. 使用 JDK HttpClient（默认）

```java
// 方式1：使用默认客户端
HttpResponse response = HttpUtils.get("https://api.example.com/users");

// 方式2：显式创建
HttpClient client = HttpUtils.createJdkHttpClient();
HttpRequest request = HttpUtils.request("https://api.example.com/users").get().build();
HttpResponse response = client.execute(request);
```

### 2. 使用 OkHttp

```java
// 方式1：使用默认配置
HttpClient client = HttpUtils.createOkHttpClient();
HttpRequest request = HttpUtils.request("https://api.example.com/users").get().build();
HttpResponse response = client.execute(request);

// 方式2：自定义配置
OkHttpClient okHttpClient = new OkHttpClient.Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(new LoggingInterceptor())
    .build();

HttpClient client = new OkHttpClientImpl(okHttpClient);
HttpResponse response = client.execute(request);
```

### 3. 带超时的客户端

```java
HttpClient client = HttpUtils.createClientWithTimeout(Duration.ofSeconds(5));
HttpRequest request = HttpUtils.request("https://api.example.com/users").get().build();
HttpResponse response = client.execute(request);
```

## 实际应用场景

### 场景1：调用第三方 API

```java
public class ExternalApiClient {
    private static final String API_BASE_URL = "https://api.example.com";
    private static final String API_KEY = "your-api-key";
    
    public List<User> getUsers(int page, int size) {
        Map<String, String> headers = Map.of("X-API-Key", API_KEY);
        Map<String, String> params = Map.of("page", String.valueOf(page), "size", String.valueOf(size));
        
        HttpResponse response = HttpUtils.get(API_BASE_URL + "/users", headers, params);
        return response.asList(User.class);  // 直接解析为 List
    }
    
    public User createUser(User user) {
        Map<String, String> headers = Map.of("X-API-Key", API_KEY);
        HttpResponse response = HttpUtils.post(API_BASE_URL + "/users", headers, user);
        return response.as(User.class);  // 直接解析为对象
    }
    
    public User getUserSafely(String userId) {
        HttpResponse response = HttpUtils.get(API_BASE_URL + "/users/" + userId);
        return response.asOrNull(User.class);  // 失败返回 null
    }
}
```

### 场景2：微服务之间调用

```java
public class OrderServiceClient {
    private final HttpClient httpClient;
    private final String baseUrl;
    
    public OrderServiceClient(String baseUrl) {
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
                    Thread.currentThread().interrupt();
                    throw new HttpClientException("Retry interrupted", e);
                }
            }
        }
        
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
