# 异常处理框架使用指南

## 概述

本框架提供了一套完整的异常处理机制，包括两种异常类型、异常码管理、异常工厂以及异常信息响应。

### 异常类型

- **CustomerException**：业务异常，用于处理业务逻辑中的异常，可以向用户展示友好的错误提示
- **SystemException**：系统异常，用于处理系统级别的异常（如数据库连接失败、配置错误等），通常不应该向用户展示详细信息

---

## 1. ExceptionCode 接口

### 作用
定义业务异常码的标准规范，所有业务异常码枚举都应实现此接口。

### 核心属性

| 方法 | 描述 | 必实现 |
|-----|-----|-------|
| `getCode()` | 获取业务错误码（如 USER_001） | 是 |
| `getDesc()` | 获取中文错误描述 | 是 |
| `getEnDesc()` | 获取英文错误描述 | 否（默认返回空） |
| `getLanguage()` | 获取语言标识（zh/en） | 否（默认返回英文） |
| `getHttpStatus()` | 获取建议的HTTP状态码 | 否（默认500） |
| `getFormatedMessage(args)` | 格式化错误消息（支持参数替换） | 否（默认实现） |

### 实现示例

```java
public enum UserExceptionCode implements ExceptionCode {
    USER_NOT_FOUND("USER_001", "用户不存在", HttpStatus.NOT_FOUND),
    USER_DISABLED("USER_002", "用户已被禁用", HttpStatus.FORBIDDEN),
    INVALID_PASSWORD("USER_003", "密码不正确", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String desc;
    private final HttpStatus httpStatus;

    UserExceptionCode(String code, String desc, HttpStatus httpStatus) {
        this.code = code;
        this.desc = desc;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
```

#### 支持国际化的实现示例

```java
@Getter
@AllArgsConstructor
public enum OrderExceptionCode implements ExceptionCode {
    ORDER_NOT_FOUND("ORDER_001", "订单不存在", "Order not found", HttpStatus.NOT_FOUND),
    ORDER_EXPIRED("ORDER_002", "订单已过期", "Order expired", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_STOCK("ORDER_003", "库存不足", "Insufficient stock", HttpStatus.CONFLICT);

    private final String code;
    private final String desc;
    private final String enDesc;
    private final HttpStatus httpStatus;

    @Override
    public String getEnDesc() {
        return enDesc;
    }
}
```

---

## 2. CustomerException 和 SystemException

### CustomerException（业务异常）

用于业务逻辑处理失败的场景，支持错误码、消息和HTTP状态码。

```java
public class CustomerException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
```

**使用场景**：
- 用户输入验证失败
- 业务规则违反
- 资源不存在
- 权限检查失败

### SystemException（系统异常）

用于系统级别的异常，通常不应向用户展示详细信息。

```java
public class SystemException extends RuntimeException {
    private final String code;
    private final String message;
}
```

**使用场景**：
- 数据库连接失败
- 配置文件错误
- 外部服务调用失败
- 内部系统错误

---

## 3. DefaultExceptionCode 枚举

框架提供了标准的HTTP异常码枚举，覆盖常见的HTTP错误场景。

| 错误码 | HTTP状态码 | 中文描述 | 英文描述 |
|------|-----------|--------|--------|
| 400 | BAD_REQUEST | 请求不合法 | Request is invalid |
| 401 | UNAUTHORIZED | 未认证 | Unauthorized |
| 403 | FORBIDDEN | 无权限 | Forbidden |
| 404 | NOT_FOUND | 资源不存在 | Not Found |
| 405 | METHOD_NOT_ALLOWED | 不支持的请求方法 | Method Not Allowed |
| 415 | UNSUPPORTED_MEDIA_TYPE | 不支持的媒体类型 | Unsupported Media Type |
| 429 | TOO_MANY_REQUESTS | 请求过于频繁 | Too Many Requests |
| 500 | COMMON_ERROR | 通用业务错误 | Internal Server Error |

---

## 4. CustomerExceptionFactory 工厂

### 作用
提供便捷的方法创建不同类型的业务异常，支持国际化和缓存。

### 主要方法

#### 4.1 使用异常码枚举创建异常

```java
// 基础用法
throw CustomerExceptionFactory.of(DefaultExceptionCode.BAD_REQUEST);

// 使用自定义枚举
throw CustomerExceptionFactory.of(UserExceptionCode.USER_NOT_FOUND);

// 支持消息参数化
throw CustomerExceptionFactory.of(OrderExceptionCode.INSUFFICIENT_STOCK, 10, 5);

// 自定义HTTP状态码
throw CustomerExceptionFactory.of(
    UserExceptionCode.USER_NOT_FOUND,
    HttpStatus.NOT_FOUND
);
```

#### 4.2 创建带消息的异常

```java
// 直接创建异常
CustomerException ex = CustomerExceptionFactory.create(
    "CUSTOM_001",
    "Custom error message",
    HttpStatus.BAD_REQUEST
);
throw ex;

// 创建业务消息异常（无需处理国际化）
throw CustomerExceptionFactory.businessMessage("操作失败，请稍后重试");
```

#### 4.3 使用国际化消息创建异常

```java
// 从数据库加载业务错误码配置
throw CustomerExceptionFactory.createException("USER_001", "John");
throw CustomerExceptionFactory.createException(HttpStatus.NOT_FOUND, "USER_001", "John");
```

### 4.4 从异常提取信息

```java
try {
    // ... 业务逻辑
} catch (Exception e) {
    ExceptionInfo info = CustomerExceptionFactory.getMessage(e);
    // info.getCode()
    // info.getMessage()
    // info.getHttpStatus()
}
```

---

## 5. ExceptionInfo 响应类

### 作用
封装异常的错误码、消息和HTTP状态码，用作API响应。

### 属性

```java
@Data
@Builder
public class ExceptionInfo {
    private String code;              // 业务错误码
    private String message;           // 错误消息
    private Integer httpStatus;       // HTTP状态码（默认500）
}
```

### 创建方法

```java
// 从异常码枚举创建
ExceptionInfo info = ExceptionInfo.of(
    DefaultExceptionCode.BAD_REQUEST,
    HttpStatus.BAD_REQUEST
);

// 从错误码和消息创建
ExceptionInfo info = ExceptionInfo.of("CUSTOM_001", "Custom message");

// 创建未知错误信息
ExceptionInfo info = ExceptionInfo.unknown("UNKNOWN_001");
```

---

## 6. BusinessCodeService 接口

### 作用
负责查询和管理业务错误码信息，支持多语言错误消息。

### 使用场景
当错误码配置存储在数据库中时，需要实现此接口。

```java
public interface BusinessCodeService {
    /**
     * 根据错误码查询对应的业务错误信息列表
     * @param code 业务错误码
     * @return 业务错误码数据列表（包含不同语言版本）
     */
    List<ExceptionCode> list(String code);
}
```

### 实现示例

```java
@Service
public class DatabaseBusinessCodeService implements BusinessCodeService {
    
    @Autowired
    private ErrorCodeRepository errorCodeRepository;
    
    @Override
    public List<ExceptionCode> list(String code) {
        return errorCodeRepository.findByCode(code);
    }
}
```

---

## 7. 完整使用示例

### 7.1 基础用法

```java
@RestController
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        User user = userService.findById(id);
        if (user == null) {
            throw CustomerExceptionFactory.of(UserExceptionCode.USER_NOT_FOUND);
        }
        return user;
    }
}
```

### 7.2 参数验证

```java
public void updateUser(String id, UserUpdateRequest request) {
    if (request.getAge() < 0 || request.getAge() > 150) {
        throw CustomerExceptionFactory.of(
            DefaultExceptionCode.BAD_REQUEST
        );
    }
    // ... 更新逻辑
}
```

### 7.3 业务规则验证

```java
public void placeOrder(String userId, OrderCreateRequest request) {
    User user = userService.findById(userId);
    if (user == null) {
        throw CustomerExceptionFactory.of(UserExceptionCode.USER_NOT_FOUND);
    }
    
    if (!user.isEnabled()) {
        throw CustomerExceptionFactory.of(UserExceptionCode.USER_DISABLED);
    }
    
    if (request.getQuantity() > availableStock) {
        throw CustomerExceptionFactory.of(
            OrderExceptionCode.INSUFFICIENT_STOCK,
            availableStock,
            request.getQuantity()
        );
    }
    // ... 创建订单
}
```

### 7.4 系统异常处理

```java
public void syncDataFromExternalService() {
    try {
        ExternalApiClient.fetchData();
    } catch (IOException e) {
        throw new SystemException("EXTERNAL_SERVICE_ERROR", 
            "Failed to fetch data from external service");
    }
}
```

### 7.5 异常处理器（全局）

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<ExceptionInfo> handleCustomerException(
            CustomerException ex) {
        ExceptionInfo info = CustomerExceptionFactory.getMessage(ex);
        return ResponseEntity
            .status(ex.getHttpStatus())
            .body(info);
    }
    
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ExceptionInfo> handleSystemException(
            SystemException ex) {
        // 记录日志
        log.error("System error: {}", ex.getMessage(), ex);
        
        ExceptionInfo info = ExceptionInfo.of(
            "SYS_ERROR",
            "Internal server error"
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(info);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionInfo> handleException(Exception ex) {
        ExceptionInfo info = CustomerExceptionFactory.getMessage(ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(info);
    }
}
```

---

## 8. 最佳实践

### 8.1 错误码设计规范

```
格式：<模块前缀>_<功能>_<错误类型>
示例：
  - USER_LOGIN_FAILED       用户模块-登录-失败
  - ORDER_PAYMENT_TIMEOUT   订单模块-支付-超时
  - INVENTORY_STOCK_EMPTY   库存模块-库存-为空
```

### 8.2 异常码枚举管理

- 每个业务模块创建独立的异常码枚举
- 错误码应该具有唯一性和可读性
- 提供清晰的中英文描述
- 选择合适的HTTP状态码

### 8.3 异常处理规则

1. **业务异常**（CustomerException）
   - 用于预期的业务错误
   - 可向用户展示
   - 选择合适的HTTP状态码

2. **系统异常**（SystemException）
   - 用于意外的系统错误
   - 记录详细日志
   - 向用户展示通用错误提示

3. **参数验证**
   - 优先使用 `DefaultExceptionCode.BAD_REQUEST`
   - 提供清晰的错误消息

### 8.4 国际化支持

```java
// 异常枚举实现国际化
public enum PaymentExceptionCode implements ExceptionCode {
    PAYMENT_FAILED(
        "PAY_001",
        "支付失败，请重试",
        "Payment failed, please retry",
        HttpStatus.BAD_REQUEST
    );
    
    // ... 实现方法
}

// 框架自动根据客户端语言返回对应的错误消息
```

---

## 9. 常见问题

### Q：CustomerException 和 SystemException 如何选择？
A：
- **CustomerException**：用户可能遇到的业务逻辑错误（参数验证失败、资源不存在等）
- **SystemException**：系统内部错误（数据库连接失败、第三方服务调用异常等）

### Q：如何实现消息参数化？
A：通过 `getFormatedMessage(args)` 方法，使用 `String.format` 格式化：
```java
public enum ErrorCode implements ExceptionCode {
    INSUFFICIENT_BALANCE("BALANCE_001", "账户余额不足，需要 %d 元，当前余额 %d 元");
    // 使用时：
    throw CustomerExceptionFactory.of(ErrorCode.INSUFFICIENT_BALANCE, 100, 50);
}
```

### Q：如何使用数据库存储的错误码？
A：实现 `BusinessCodeService` 接口并注册到 `CustomerExceptionFactory`：
```java
@Autowired
public void init(BusinessCodeService service) {
    CustomerExceptionFactory.setBusinessCodeService(service);
}
```

