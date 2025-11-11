### 依赖配置

在业务工程中引入依赖（先在根目录执行 `mvn clean install` 安装本模块到本地仓库）：

```xml
<dependency>
  <groupId>com.qbit.framework</groupId>
  <artifactId>openapi-auth-starter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

示例配置（`application.yml`），请根据实际实现调整键名：

```yaml
openapi:
  auth:
    base-url: https://api.example.com
    clientId: YOUR_CLIENT_ID

```

> 需要启用 Redis 
### 使用示例

示例：通过工厂获得 `OpenApiClient` 并调用外部 API，鉴权与令牌管理由 Starter 统一处理。

```java
import org.springframework.stereotype.Service;
import java.util.UUID;
import money.interlace.sdk.api.BudgetsApi;
import money.interlace.sdk.client.OpenApiClient;
import money.interlace.sdk.client.OpenApiClientFactory;
import money.interlace.sdk.model.BudgetResponse;
import money.interlace.sdk.ApiException;

@Service
public class BudgetService {
    private final OpenApiClient client;

    public BudgetService(OpenApiClientFactory factory) {
        // 通过工厂创建/获取客户端；Starter 已为请求附加鉴权拦截器
        this.client = new OpenApiClient(factory);
    }

    public BudgetResponse getBudget(UUID budgetId, UUID merchantId) {
        return client.execute(
            BudgetsApi.class,
            api -> {
                try {
                    return api.getBudget(budgetId, merchantId);
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        );
    }
}
```

如需改用基于 `OkHttpClient` 的请求方式，本 Starter 已集成拦截器，你也可以直接注入 `OkHttpClient` 并发起请求。
