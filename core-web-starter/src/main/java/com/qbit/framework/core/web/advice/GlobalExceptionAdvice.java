package com.qbit.framework.core.web.advice;

import com.qbit.framework.core.api.model.web.Result;
import com.qbit.framework.core.toolkits.exception.code.DefaultExceptionCode;
import com.qbit.framework.core.toolkits.exception.factory.CustomerExceptionFactory;
import com.qbit.framework.core.toolkits.exception.factory.ExceptionInfo;
import com.qbit.framework.core.toolkits.exception.CustomerException;
import com.qbit.framework.core.toolkits.exception.SystemException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理切面
 * 
 * <p>统一处理应用中的所有异常，将异常转换为标准的 {@link Result} 响应格式。
 * 
 * <p>异常处理策略：
 * <ul>
 *   <li>{@link CustomerException} - 业务异常，返回完整错误信息给客户端</li>
 *   <li>{@link SystemException} - 系统异常，隐藏详细错误，仅返回通用提示</li>
 *   <li>参数校验异常 - 收集所有校验错误，返回友好提示</li>
 *   <li>其他未知异常 - 记录详细日志，返回通用错误提示</li>
 * </ul>
 * 
 * <p>注意：此类不会自动注册，需要使用方主动导入。
 * 使用方式：在配置类中添加 {@code @Import(GlobalExceptionAdvice.class)}
 *
 * @author Qbit Framework
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 处理业务异常
     * CustomerException 是可预期的业务异常，可以安全地将错误信息返回给客户端
     */
    @ExceptionHandler(CustomerException.class)
    public ResponseEntity<Result<Object>> handleCustomerException(CustomerException e) {
        // 利用异常对象直接获取所有信息
        HttpStatus httpStatus = e.getHttpStatus();
        String code = e.getCode();
        String message = e.getMessage();
        
        // 根据异常类型选择合适的日志级别
        if (httpStatus.is4xxClientError()) {
            // 客户端错误（如参数错误、权限不足）- WARN级别，记录堆栈
            log.warn("Client error - code: {}, message: {}, status: {}", code, message, httpStatus.value(), e);
        } else {
            // 服务端错误 - ERROR级别，记录堆栈
            log.error("Business error - code: {}, message: {}, status: {}", code, message, httpStatus.value(), e);
        }
        
        return buildResponse(httpStatus, code, message);
    }

    /**
     * 处理系统异常
     * SystemException 是系统级异常（如数据库故障、配置错误），不应暴露详细信息给客户端
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Object>> handleSystemException(SystemException e) {
        String code = e.getCode();
        
        // 系统异常需要记录详细堆栈信息，便于排查问题
        log.error("System error - code: {}, message: {}", code, e.getMessage(), e);
        
        // 返回通用错误提示，隐藏系统内部细节
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                code,
                "系统繁忙，请稍后重试"
        );
    }

    /**
     * 处理Bean校验异常（@Valid 注解触发）
     * 如：Controller方法参数上的 @Valid User user
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<Result<Object>> handleValidationException(Exception e) {
        BindingResult bindingResult = e instanceof MethodArgumentNotValidException
                ? ((MethodArgumentNotValidException) e).getBindingResult()
                : ((BindException) e).getBindingResult();
        
        // 收集所有校验错误消息
        String message = bindingResult.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("Validation failed: {}", message, e);
        
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                DefaultExceptionCode.BAD_REQUEST.getCode(),
                message
        );
    }

    /**
     * 处理参数约束校验异常
     * 如：Controller方法参数上的 @NotBlank String name
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Object>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        log.warn("Constraint violation: {}", message, e);
        
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                DefaultExceptionCode.BAD_REQUEST.getCode(),
                message
        );
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage(), e);
        
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                DefaultExceptionCode.BAD_REQUEST.getCode(),
                e.getMessage() != null ? e.getMessage() : "参数不合法"
        );
    }

    /**
     * 处理非法状态异常
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Result<Object>> handleIllegalStateException(IllegalStateException e) {
        log.error("Illegal state: {}", e.getMessage(), e);
        
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                DefaultExceptionCode.COMMON_ERROR.getCode(),
                "系统状态异常，请稍后重试"
        );
    }

    /**
     * 处理资源未找到异常
     * 如：访问不存在的URL路径或静态资源
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Object>> handleNoResourceFoundException(
            NoResourceFoundException e) {
        log.warn("Resource not found: {}", e.getResourcePath());
        
        return buildResponse(
                HttpStatus.NOT_FOUND,
                DefaultExceptionCode.NOT_FOUND.getCode(),
                "请求的资源不存在"
        );
    }

    /**
     * 处理HTTP请求方法不支持异常
     * 如：接口只支持POST但使用了GET请求
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("HTTP method not supported: {} for {}", e.getMethod(), e.getMessage());
        
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                DefaultExceptionCode.METHOD_NOT_ALLOWED.getCode(),
                String.format("不支持的请求方法: %s", e.getMethod())
        );
    }

    /**
     * 处理媒体类型不支持异常
     * 如：接口要求application/json但发送了application/xml
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result<Object>> handleHttpMediaTypeNotSupportedException(
            HttpMediaTypeNotSupportedException e) {
        log.warn("Media type not supported: {}", e.getContentType());
        
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                DefaultExceptionCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                "不支持的媒体类型，请使用正确的Content-Type"
        );
    }

    /**
     * 处理缺少请求参数异常
     * 如：接口要求参数userId但请求中未提供
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("Missing request parameter: {} of type {}", e.getParameterName(), e.getParameterType());
        
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                DefaultExceptionCode.BAD_REQUEST.getCode(),
                String.format("缺少必需参数: %s", e.getParameterName())
        );
    }

    /**
     * 处理方法参数类型不匹配异常
     * 如：参数要求Integer但传入了非数字字符串
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter: {}, required type: {}, provided value: {}", 
                e.getName(), 
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown",
                e.getValue());
        
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                DefaultExceptionCode.BAD_REQUEST.getCode(),
                String.format("参数类型错误: %s 应为 %s 类型", 
                        e.getName(), 
                        e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知")
        );
    }

    /**
     * 处理空指针异常
     * 这通常是代码缺陷，需要重点关注和修复
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Object>> handleNullPointerException(NullPointerException e) {
        log.error("NullPointerException occurred - this indicates a code defect that should be fixed", e);
        
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                DefaultExceptionCode.COMMON_ERROR.getCode(),
                "系统内部错误，请稍后重试"
        );
    }

    /**
     * 兜底异常处理
     * 捕获所有未被上述方法处理的异常，避免异常信息直接泄露给客户端
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleUnexpectedException(Exception e) {
        // 使用工厂方法提取异常信息
        ExceptionInfo exceptionInfo = CustomerExceptionFactory.getMessage(e);
        
        // 记录详细的异常堆栈，便于排查问题
        log.error("Unexpected exception - code: {}, message: {}, type: {}", 
                exceptionInfo.getCode(), 
                exceptionInfo.getMessage(),
                e.getClass().getName(), 
                e);
        
        // 返回通用错误提示，避免泄露系统内部信息
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exceptionInfo.getCode(),
                "系统繁忙，请稍后重试"
        );
    }

    /**
     * 构建统一的响应格式
     * 
     * @param httpStatus HTTP状态码
     * @param code 业务错误码
     * @param message 错误消息
     * @return 响应实体
     */
    private ResponseEntity<Result<Object>> buildResponse(HttpStatus httpStatus, String code, String message) {
        Result<Object> result = Result.fail(code, message);
        return ResponseEntity.status(httpStatus).body(result);
    }
}
