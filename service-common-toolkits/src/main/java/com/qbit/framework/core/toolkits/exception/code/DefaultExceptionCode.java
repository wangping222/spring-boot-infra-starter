package com.qbit.framework.core.toolkits.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 默认异常码枚举
 * 提供通用的HTTP异常码定义，涵盖常见的HTTP错误场景
 *
 * <p>这个枚举直接实现 ExceptionCode 接口，用于处理标准的HTTP错误
 * 对于业务特定的错误码，应该创建自己的异常码枚举实现 ExceptionCode 接口
 *
 * <p>使用示例：
 * <pre>
 * // 创建异常
 * throw CustomerExceptionFactory.of(DefaultExceptionCode.BAD_REQUEST);
 * throw CustomerExceptionFactory.of(DefaultExceptionCode.UNAUTHORIZED);
 * 
 * // 使用自定义异常码
 * public enum UserExceptionCode implements ExceptionCode {
 *     USER_NOT_FOUND("USER_001", "用户不存在", HttpStatus.NOT_FOUND),
 *     USER_DISABLED("USER_002", "用户已被禁用", HttpStatus.FORBIDDEN);
 *     
 *     // ... 实现接口
 * }
 * throw CustomerExceptionFactory.of(UserExceptionCode.USER_NOT_FOUND);
 * </pre>
 *
 * @author Qbit Framework
 */
@Getter
@AllArgsConstructor
public enum DefaultExceptionCode implements ExceptionCode {

    /**
     * 请求不合法 - 客户端请求参数错误、格式错误等
     */
    BAD_REQUEST("400", "请求不合法", "Request is invalid", HttpStatus.BAD_REQUEST),

    /**
     * 未认证 - 用户未登录或token无效
     */
    UNAUTHORIZED("401", "未认证", "Unauthorized", HttpStatus.UNAUTHORIZED),

    /**
     * 无权限 - 用户已认证但没有访问权限
     */
    FORBIDDEN("403", "无权限", "Forbidden", HttpStatus.FORBIDDEN),

    /**
     * 资源不存在 - 请求的资源找不到
     */
    NOT_FOUND("404", "资源不存在", "Not Found", HttpStatus.NOT_FOUND),

    /**
     * 不支持的请求方法 - 如接口只支持POST但使用了GET
     */
    METHOD_NOT_ALLOWED("405", "不支持的请求方法", "Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED),

    /**
     * 不支持的媒体类型 - 如要求JSON但发送了XML
     */
    UNSUPPORTED_MEDIA_TYPE("415", "不支持的媒体类型", "Unsupported Media Type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),

    /**
     * 请求过于频繁 - 触发限流规则
     */
    TOO_MANY_REQUESTS("429", "请求过于频繁", "Too Many Requests", HttpStatus.TOO_MANY_REQUESTS),

    /**
     * 通用业务错误 - 业务逻辑处理失败
     */
    COMMON_ERROR("500", "通用业务错误", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误描述（中文）
     */
    private final String desc;

    /**
     * 错误描述（英文）
     */
    private final String enDesc;

    /**
     * HTTP状态码
     */
    private final HttpStatus httpStatus;
}
