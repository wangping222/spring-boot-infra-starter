# 异常处理使用指南

## 概述

经过优化后的异常处理体系提供了更灵活、更易用的API，支持多种创建方式和链式调用。

## 核心组件

### 1. CustomerException - 业务异常
用于处理业务逻辑中的异常情况，支持自定义错误码、消息和HTTP状态码。

### 2. SystemException - 系统异常
用于处理系统级别的异常（数据库连接、配置错误等），不应向用户展示详细信息。

### 3. CustomerExceptionFactory - 异常工厂
提供便捷的静态方法创建各种类型的业务异常。

### 4. ExceptionInfo - 异常信息
封装异常的错误码、消息和HTTP状态码，可作为API响应。

## 使用方式

### 方式1：使用工厂方法（推荐）

```java
// 创建400错误
throw CustomerExceptionFactory.badRequest("INVALID_PARAM", userId);

// 创建401错误
throw CustomerExceptionFactory.unauthorized("TOKEN_EXPIRED");

// 创建403错误
throw CustomerExceptionFactory.forbidden("NO_PERMISSION", resource);

// 创建404错误
throw CustomerExceptionFactory.notFound("USER_NOT_FOUND", userId);

// 创建429错误
throw CustomerExceptionFactory.tooManyRequests("RATE_LIMIT_EXCEEDED");

// 创建500错误
throw CustomerExceptionFactory.internalError("DATABASE_ERROR");
```

### 方式2：使用枚举（类型安全）

```java
// 直接使用枚举
throw CustomerExceptionFactory.of(DefaultExceptionCode.BAD_REQUEST, HttpStatus.BAD_REQUEST);

// 使用枚举构建器（可自定义消息）
throw CustomerExceptionFactory.of(DefaultExceptionCode.UNAUTHORIZED)
    .message("您的会话已过期，请重新登录")
    .httpStatus(HttpStatus.UNAUTHORIZED)
    .build();
```

### 方式3：使用Builder模式（最灵活）

```java
// 完全自定义
throw CustomerException.builder()
    .code("CUSTOM_ERROR")
    .message("自定义错误消息")
    .httpStatus(HttpStatus.FORBIDDEN)
    .build();

// 使用枚举 + 自定义消息
throw CustomerException.builder()
    .code(DefaultExceptionCode.COMMON_ERROR)
    .message("用户 " + username + " 操作失败")
    .httpStatus(HttpStatus.BAD_REQUEST)
    .build();
```

### 方式4：直接创建

```java
// 简单创建
throw new CustomerException("操作失败");

// 完整参数
throw new CustomerException("INVALID_DATA", "数据格式不正确", HttpStatus.BAD_REQUEST);

// 使用工厂直接创建
throw CustomerExceptionFactory.create("CUSTOM_CODE", "自定义消息", HttpStatus.CONFLICT);
```

### 方式5：带消息参数（国际化）

```java
// 支持占位符，从数据库加载国际化消息
throw CustomerExceptionFactory.badRequest("USER_AGE_INVALID", minAge, maxAge);
// 消息模板：用户年龄必须在 {0} 到 {1} 之间
```

## 系统异常使用

```java
// 基础用法
throw new SystemException("配置文件加载失败");

// 带错误码
throw new SystemException("SYS_DB_001", "数据库连接失败");

// 带原始异常
try {
    // 数据库操作
} catch (SQLException e) {
    throw new SystemException("数据库查询失败", e);
}

// 完整参数
try {
    // 配置加载
} catch (IOException e) {
    throw new SystemException("SYS_CONFIG_001", "配置文件读取失败", e);
}
```

## 异常信息提取

```java
// 从异常提取信息
ExceptionInfo info = CustomerExceptionFactory.getMessage(exception);

// 获取国际化消息
ExceptionInfo info = CustomerExceptionFactory.getMessage("USER_NOT_FOUND", userId);

// 构建响应
return ResponseEntity
    .status(info.getHttpStatus())
    .body(info);
```

## 最佳实践

### 1. 业务异常 vs 系统异常

```java
// ✅ 业务异常 - 用户可理解的错误
if (user == null) {
    throw CustomerExceptionFactory.notFound("USER_NOT_FOUND", userId);
}

// ✅ 系统异常 - 技术性错误
try {
    configLoader.load();
} catch (IOException e) {
    throw new SystemException("CONFIG_LOAD_FAILED", "配置加载失败", e);
}
```

### 2. 选择合适的HTTP状态码

```java
// 400 - 客户端请求错误
throw CustomerExceptionFactory.badRequest("INVALID_EMAIL", email);

// 401 - 未认证
throw CustomerExceptionFactory.unauthorized("TOKEN_INVALID");

// 403 - 已认证但无权限
throw CustomerExceptionFactory.forbidden("INSUFFICIENT_PERMISSION");

// 404 - 资源不存在
throw CustomerExceptionFactory.notFound("RESOURCE_NOT_FOUND", resourceId);

// 429 - 请求过于频繁
throw CustomerExceptionFactory.tooManyRequests("RATE_LIMIT");

// 500 - 服务器内部错误
throw CustomerExceptionFactory.internalError("UNEXPECTED_ERROR");
```

### 3. 使用枚举管理错误码

```java
public enum OrderExceptionCode implements ExceptionCode {
    ORDER_NOT_FOUND("ORD_001", "订单不存在"),
    ORDER_CANCELLED("ORD_002", "订单已取消"),
    ORDER_PAID("ORD_003", "订单已支付"),
    INVALID_ORDER_STATUS("ORD_004", "订单状态不正确");

    private final String code;
    private final String desc;

    // constructor, getters...
}

// 使用
throw CustomerExceptionFactory.of(OrderExceptionCode.ORDER_NOT_FOUND, HttpStatus.NOT_FOUND);
```

### 4. 国际化支持

在数据库中配置错误码的多语言消息：

```sql
INSERT INTO business_code (code, language, message_template) VALUES
('USER_AGE_INVALID', 'zh', '用户年龄必须在 {0} 到 {1} 之间'),
('USER_AGE_INVALID', 'en', 'User age must be between {0} and {1}');
```

使用时：
```java
throw CustomerExceptionFactory.badRequest("USER_AGE_INVALID", 18, 65);
// 中文环境：用户年龄必须在 18 到 65 之间
// 英文环境：User age must be between 18 and 65
```

## 优化亮点

1. **统一错误码**：解决了之前错误码不一致的问题
2. **Builder模式**：支持链式调用，灵活构建异常
3. **枚举支持**：类型安全，避免硬编码
4. **多种创建方式**：简单场景用工厂方法，复杂场景用Builder
5. **HTTP状态码集成**：异常与HTTP响应无缝对接
6. **国际化支持**：从数据库加载多语言消息
7. **完整注释**：所有类和方法都有详细的JavaDoc
8. **职责分离**：业务异常和系统异常明确区分

## 迁移指南

### 旧代码
```java
throw new CustomerException(
    DefaultExceptionCode.COMMON_ERROR.getCode(), 
    "操作失败", 
    HttpStatus.INTERNAL_SERVER_ERROR
);
```

### 新代码（多种选择）
```java
// 选项1：使用工厂
throw CustomerExceptionFactory.businessMessage("操作失败");

// 选项2：使用Builder
throw CustomerException.builder()
    .code(DefaultExceptionCode.COMMON_ERROR)
    .message("操作失败")
    .build();

// 选项3：使用枚举
throw CustomerExceptionFactory.of(DefaultExceptionCode.COMMON_ERROR)
    .message("操作失败")
    .build();
```
