package com.qbit.framework.core.toolkits.exception.type;

import lombok.Getter;

/**
 * 系统异常类
 * 用于处理系统级别的异常，如数据库连接失败、配置错误等非业务异常
 * 与 CustomerException 的区别：
 * - SystemException: 系统级异常，通常不应该向用户展示详细信息
 * - CustomerException: 业务异常，可以向用户展示友好的错误提示
 *
 * @author Qbit Framework
 */
@Getter
public class SystemException extends RuntimeException {

    /**
     * 系统错误码
     */
    private final String code;
    
    /**
     * 错误消息
     */
    private final String message;

    /**
     * 构造系统异常
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public SystemException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造系统异常（无错误码）
     *
     * @param message 错误消息
     */
    public SystemException(String message) {
        super(message);
        this.code = "SYS_ERROR";
        this.message = message;
    }

    /**
     * 构造系统异常（带原始异常）
     *
     * @param message 错误消息
     * @param cause   原始异常
     */
    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = "SYS_ERROR";
        this.message = message;
    }

    /**
     * 构造系统异常（完整参数）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param cause   原始异常
     */
    public SystemException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
}
