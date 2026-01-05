package com.qbit.framework.core.toolkits.exception.factory;

import com.qbit.framework.core.toolkits.exception.code.ExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 异常信息响应类
 * 用于封装异常的错误码、消息和HTTP状态码，可作为API响应的一部分
 *
 * @author Qbit Framework
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionInfo {

    /**
     * 业务错误码
     */
    private String code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * HTTP状态码
     */
    @Builder.Default
    private Integer httpStatus = HttpStatus.INTERNAL_SERVER_ERROR.value();

    /**
     * 创建未知错误码的异常信息
     *
     * @param code 未识别的错误码
     * @return 异常信息对象
     */
    public static ExceptionInfo unknown(String code) {
        return ExceptionInfo.builder()
                .code("999999")
                .message("Unknown error code: " + code)
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

    /**
     * 从异常码枚举创建异常信息
     *
     * @param exceptionCode 异常码枚举
     * @param httpStatus    HTTP状态码
     * @return 异常信息对象
     */
    public static ExceptionInfo of(ExceptionCode exceptionCode, HttpStatus httpStatus, Object... args) {
        return ExceptionInfo.builder()
                .code(exceptionCode.getCode())
                .message(exceptionCode.getFormatedMessage(args))
                .httpStatus(httpStatus.value())
                .build();
    }

    /**
     * 创建简单的异常信息
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 异常信息对象
     */
    public static ExceptionInfo of(String code, String message) {
        return ExceptionInfo.builder()
                .code(code)
                .message(message)
                .build();
    }
}
