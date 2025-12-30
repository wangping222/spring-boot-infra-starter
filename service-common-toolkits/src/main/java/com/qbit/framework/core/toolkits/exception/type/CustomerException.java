package com.qbit.framework.core.toolkits.exception.type;

import com.qbit.framework.core.toolkits.exception.code.ExceptionCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况，支持自定义错误码、消息和HTTP状态码
 *
 * @author Qbit Framework
 */
@Getter
public class CustomerException extends RuntimeException {

    /**
     * 默认错误码
     */
    public static final String DEFAULT_ERROR_CODE = "999999";

    /**
     * HTTP响应状态码
     */
    private final HttpStatus httpStatus;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 业务错误码
     */
    private final String code;

    /**
     * 构造业务异常
     *
     * @param code       业务错误码
     * @param message    错误消息
     * @param httpStatus HTTP状态码
     */
    public CustomerException(String code, String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    /**
     * 使用默认错误码和状态码构造异常
     *
     * @param message 错误消息
     */
    public CustomerException(String message) {
        super(message);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = DEFAULT_ERROR_CODE;
        this.message = message;
    }

    /**
     * 使用异常码枚举构造异常
     *
     * @param exceptionCode 异常码枚举
     * @param httpStatus    HTTP状态码
     */
    public CustomerException(ExceptionCode exceptionCode, HttpStatus httpStatus) {
        super(exceptionCode.getDesc());
        this.code = exceptionCode.getCode();
        this.message = exceptionCode.getDesc();
        this.httpStatus = httpStatus;
    }

    /**
     * 创建异常构建器
     *
     * @return 异常构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 业务异常构建器
     * 支持链式调用，灵活构建异常对象
     */
    public static class Builder {
        private String code = DEFAULT_ERROR_CODE;
        private String message;
        private HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        /**
         * 设置错误码
         */
        public Builder code(String code) {
            this.code = code;
            return this;
        }

        /**
         * 使用异常码枚举设置错误码和消息
         */
        public Builder code(ExceptionCode exceptionCode) {
            this.code = exceptionCode.getCode();
            if (this.message == null) {
                this.message = exceptionCode.getDesc();
            }
            return this;
        }

        /**
         * 设置错误消息
         */
        public Builder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置HTTP状态码
         */
        public Builder httpStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        /**
         * 构建异常对象
         */
        public CustomerException build() {
            if (message == null) {
                message = "Business exception";
            }
            return new CustomerException(code, message, httpStatus);
        }
    }
}
