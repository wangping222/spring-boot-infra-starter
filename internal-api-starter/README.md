internal-api-starter 提供基于 OpenFeign 的统一签名拦截器、超时与日志配置，用于在调用内部接口时自动添加签名请求头，并支持统一 `base-url`。

## 快速开始

- 引入依赖
```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>internal-api-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

- 应用入口启用客户端扫描
```java
import com.qbit.framework.starter.merchant.EnableInternalApiClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableInternalApiClients
public class DemoApplication {}
```

- 配置（application.yml）
```yaml
framework:
  feign:
    api:
      enabled: true
      base-url: https://api.example.internal
      secret: YOUR_SECRET
      use-okhttp: true
      connect-timeout-millis: 5000
      read-timeout-millis: 10000
      log-enabled: true
      log-headers: true
      log-body: false
      log-body-max-bytes: 2048
```

## 行为说明

- 签名拦截：自动为请求附加签名头（`x-sign`、`x-nonce-str`、`x-timestamp`），当 `framework.feign.api.secret` 存在时生效。
- 统一地址：当客户端未设置 `@FeignClient(url=...)` 且配置了 `framework.feign.api.base-url` 时，为该客户端绑定统一地址。
- 超时配置：
  - Feign Options：`framework.feign.api.connect-timeout-millis`、`framework.feign.api.read-timeout-millis`
  - OkHttp Client：与上述保持一致
- 日志输出：
  - 启用 `framework.feign.api.log-enabled` 后，输出单次请求/响应的聚合日志（一次 `log.info`，多行内容）。
  - 由 `log-headers`/`log-body` 决定日志级别（HEADERS/BODY/BASIC），敏感头默认脱敏。

## 可扩展与覆盖

- 可覆盖 Bean：
  - `Request.Options`（`@ConditionalOnMissingBean`）
  - `feign.Client`（OkHttp）（`@ConditionalOnMissingBean`）
- 仍可自定义 `@EnableInternalApiClients(basePackages = { ... })` 控制扫描范围。
