XXL-Job Starter：为 Spring Boot 提供自动装配与增强使用体验

### 依赖

```xml
<dependency>
    <groupId>com.qbit.framework</groupId>
    <artifactId>xxljob-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置

```yaml
xxl:
  job:
    enabled: true
    adminAddresses: http://xxl-job-admin:8080/xxl-job-admin
    accessToken: xxx
    appname: demo-executor
    ip: 127.0.0.1
    port: 9999
    logPath: /data/applogs/xxl-job/
    logRetentionDays: 30
```

### 使用示例

```java
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class DemoJob {
    @XxlJob("demoJob")
    public void run() {
        System.out.println("run xxl-job");
    }
}
```

### 增强点

- 支持开关 `xxl.job.enabled`
- 校验 `adminAddresses` 与 `appname` 必填
- 提供 `XxlJobExecutorCustomizer` 扩展点，可定制执行器参数

