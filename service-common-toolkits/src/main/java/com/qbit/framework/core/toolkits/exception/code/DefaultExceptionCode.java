package com.qbit.framework.core.toolkits.exception.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 默认异常码枚举
 * 提供通用的异常码定义，涵盖常见的HTTP错误场景
 * 
 * <p>各模块可以定义自己的异常码枚举来扩展更具体的业务错误
 *
 * @author Qbit Framework
 */
@Getter
@AllArgsConstructor
public enum DefaultExceptionCode implements ExceptionCode {

    /**
     * 请求不合法 - 客户端请求参数错误、格式错误等
     */
    BAD_REQUEST("400", "请求不合法", HttpStatus.BAD_REQUEST),

    /**
     * 未认证 - 用户未登录或token无效
     */
    UNAUTHORIZED("401", "未认证", HttpStatus.UNAUTHORIZED),

    /**
     * 无权限 - 用户已认证但没有访问权限
     */
    FORBIDDEN("403", "无权限", HttpStatus.FORBIDDEN),

    /**
     * 资源不存在 - 请求的资源找不到
     */
    NOT_FOUND("404", "资源不存在", HttpStatus.NOT_FOUND),

    /**
     * 请求过于频繁 - 触发限流规则
     */
    TOO_MANY_REQUESTS("429", "请求过于频繁", HttpStatus.TOO_MANY_REQUESTS),

    /**
     * 通用业务错误 - 业务逻辑处理失败
     */
    COMMON_ERROR("500", "通用业务错误", HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误描述
     */
    private final String desc;

    /**
     * HTTP状态码
     */
    private final HttpStatus httpStatus;
}
