package com.qbit.framework.core.web.handler;

import com.qbit.framework.core.api.model.toolkits.exception.code.DefaultExceptionCode;
import com.qbit.framework.core.api.model.toolkits.exception.type.CustomerException;
import com.qbit.framework.core.api.model.toolkits.exception.type.SystemException;
import com.qbit.framework.core.api.model.web.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理
 *
 * 统一将异常转换为 {@link Result} 响应，
 * 业务异常优先使用 {@link CustomerException}，
 * 其他未捕获异常统一转换为通用错误码。
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<Result<Object>> handleCustomerException(CustomerException e) {
        HttpStatus status = e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR;
        String code = e.getCode() != null ? e.getCode() : DefaultExceptionCode.COMMON_ERROR.getCode();
        String message = e.getMessage();
        // 记录完整异常信息，方便排查问题
        log.warn("Business exception: code={}, message={}", code, message);
        // CustomerException 是业务异常，可以返回给客户端
        return buildResponse(status, code, message);
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Object>> handleSystemException(SystemException e) {
        String code = e.getCode() != null ? e.getCode() : DefaultExceptionCode.COMMON_ERROR.getCode();
        // 记录完整异常信息，方便排查问题
        log.error("System exception: code={}, message={}", code, e.getMessage(), e);
        // SystemException 是系统异常，不应返回详细错误信息给客户端
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, code, "系统内部错误");
    }

    /**
     * Bean 校验异常（如 @Valid 对象参数）
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Object>> handleBindException(Exception e) {
        BindingResult bindingResult;
        if (e instanceof MethodArgumentNotValidException exception) {
            bindingResult = exception.getBindingResult();
        } else {
            bindingResult = ((BindException) e).getBindingResult();
        }
        String message = bindingResult.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, DefaultExceptionCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 单参数校验异常（如 @NotBlank String param）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Object>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, DefaultExceptionCode.BAD_REQUEST.getCode(), message);
    }

    /**
     * 兜底异常处理，避免异常直接泄露到客户端。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                DefaultExceptionCode.COMMON_ERROR.getCode(),
                "系统内部错误");
    }

    private ResponseEntity<Result<Object>> buildResponse(HttpStatus status, String code, String message) {
        Result<Object> result = Result.fail(code, message);
        return ResponseEntity.status(status).body(result);
    }
}
