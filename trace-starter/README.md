Trace Starter：提供 MDC 封装、Trace 工具与 Web 自动传播

### 依赖

```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>trace-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置

```yaml
trace:
  enabled: true
  web-enabled: true
```

### 使用

- try-with-resources 作用域：

```java
import com.qbit.framework.business.trace.starter.core.MdcScope;
import com.qbit.framework.business.trace.starter.TraceUtils;

try (var scope = MdcScope.with("traceId", TraceUtils.newId())) {
    // 业务逻辑，日志自动带上 traceId
}
```

- Web 自动传播：
  - 读取 `X-Trace-Id/Trace-Id` 与 `X-Span-Id/Span-Id`，无则自动生成
  - 请求结束清理 MDC

- 工具方法：
  - `TraceUtils.newId()` 生成 ID
  - `TraceUtils.setTrace(traceId, spanId)` 设置上下文
  - `TraceUtils.current()` 获取当前上下文 Map

