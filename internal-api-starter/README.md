internal-api-starter 提供基于 OpenFeign 的统一签名拦截器与超时配置，用于在调用内部接口时自动添加资产签名请求头。

## 使用步骤

1. 引入依赖

```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>internal-api-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

2. 配置账号与密钥（application.yml）

```yaml
feign:
  api:
    base-url: https://api.example.internal
    account-id: YOUR_ACCOUNT_ID
    secret: YOUR_SECRET
    connect-timeout-millis: 5000
    read-timeout-millis: 10000
    use-okhttp: true
```

3. 启用 Feign

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}
```

4. 使用服务端提供的客户端 Jar

服务提供方会发布包含 `@FeignClient` 声明的客户端接口 Jar，业务方无需自行创建接口，只需：

```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>merchant-api-clients</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

在应用入口启用 Feign 扫描该包：

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.qbit.merchant.clients")
public class DemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
 }
}
```

直接注入并调用客户端接口：

```java
import com.qbit.merchant.clients.MerchantApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {
  private final MerchantApiClient client;

  public String ping() {
    return client.ping();
  }
}
```

5. 发起调用（签名头会自动添加，且未显式配置 url 的客户端将自动绑定到 `feign.api.base-url`）

```java
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {
  private final MerchantApiClient client;

  public String ping() {
    return client.ping();
  }
}
```

## 说明

- 拦截器会根据请求方法与路径生成签名并注入到请求头中（`nonceStr`、`timestamp`、`sign`、`account-id` 等）。
- 超时通过 `feign.api.connect-timeout-millis` 与 `feign.api.read-timeout-millis` 配置。
- 仅在配置了 `feign.api.account-id` 与 `feign.api.secret` 时拦截器生效。
- 当 `feign.api.base-url` 配置存在且客户端未设置 `@FeignClient(url=...)` 时，系统会自动为该客户端绑定 `base-url`。

## 相关实现位置

- 签名拦截器与超时配置：`internal-api-starter/src/main/java/com/qbit/framework/business/merchant/starter/config/FeignAutoConfiguration.java:20`
- 签名头构造：`service-starter/src/main/java/com/qbit/framework/business/service/starter/request/HeaderUtils.java:36`