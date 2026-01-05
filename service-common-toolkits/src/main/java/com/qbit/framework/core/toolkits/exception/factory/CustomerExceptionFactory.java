package com.qbit.framework.core.toolkits.exception.factory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qbit.framework.core.toolkits.exception.code.BusinessCodeService;
import com.qbit.framework.core.toolkits.exception.code.DefaultExceptionCode;
import com.qbit.framework.core.toolkits.exception.code.ExceptionCode;
import com.qbit.framework.core.toolkits.exception.CustomerException;
import com.qbit.framework.core.toolkits.i18n.I18nMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 客户异常工厂类
 * 提供便捷的方法创建不同类型的业务异常，支持国际化和缓存
 *
 * <p>使用示例：
 * <pre>
 * // 基础用法
 * throw CustomerExceptionFactory.badRequest("INVALID_PARAM", userId);
 *
 * // 使用枚举
 * throw CustomerExceptionFactory.of(DefaultExceptionCode.BAD_REQUEST)
 *     .httpStatus(HttpStatus.BAD_REQUEST)
 *     .build();
 *
 * // 直接创建
 * throw CustomerExceptionFactory.create("CUSTOM_ERROR", "Custom message", HttpStatus.FORBIDDEN);
 * </pre>
 *
 * @author Qbit Framework
 */
@Slf4j
public class CustomerExceptionFactory {

    /**
     * 默认错误码
     */
    public static final String DEFAULT_ERROR_CODE = CustomerException.DEFAULT_ERROR_CODE;

    /**
     * 业务错误码缓存
     */
    private static final Cache<String, List<ExceptionCode>> BUSINESS_CODE_CACHE =
            Caffeine.newBuilder()
                    .maximumSize(2048)
                    .expireAfterWrite(Duration.ofMinutes(10))
                    .build();

    private static BusinessCodeService businessCodeService;

    private CustomerExceptionFactory() {
    }

    public static void setBusinessCodeService(BusinessCodeService businessCodeService) {
        CustomerExceptionFactory.businessCodeService = businessCodeService;
    }

    /**
     * 创建带消息的业务异常
     *  message 需要自己处理国际化
     */
    public static CustomerException businessMessage(String message) {
        return CustomerException.builder()
                .code(DefaultExceptionCode.COMMON_ERROR.getCode())
                .message(message != null ? message : "Business exception")
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
    }

    // ==================== 使用枚举创建 ====================

    /**
     * 使用异常码枚举创建异常
     *
     * @param exceptionCode 异常码枚举
     * @return 业务异常
     */
    public static CustomerException of(ExceptionCode exceptionCode, String... args) {
        String formatedMessage = exceptionCode.getFormatedMessage(args);
        return CustomerException.builder()
                .code(exceptionCode)
                .message(formatedMessage)
                .httpStatus(exceptionCode.getHttpStatus())
                .build();
    }

    /**
     * 使用异常码枚举和HTTP状态码创建异常（可自定义HTTP状态码）
     *
     * @param exceptionCode 异常码枚举
     * @param httpStatus HTTP状态码
     * @return 业务异常
     */
    public static CustomerException of(ExceptionCode exceptionCode, HttpStatus httpStatus, String... args) {
        String formatedMessage = exceptionCode.getFormatedMessage(args);
        return CustomerException.builder()
                .code(exceptionCode)
                .message(formatedMessage)
                .httpStatus(httpStatus)
                .build();
    }

    // ==================== 直接创建异常 ====================

    /**
     * 直接创建异常
     *
     * @param code 错误码
     * @param message 错误消息
     * @param httpStatus HTTP状态码
     * @return 业务异常
     */
    public static CustomerException create(String code, String message, HttpStatus httpStatus) {
        return new CustomerException(code, message, httpStatus);
    }

    /**
     * 创建异常（使用国际化消息）
     *
     * @param status HTTP状态码
     * @param code 错误码
     * @param args 消息参数
     * @return 业务异常
     */
    public static CustomerException createException(HttpStatus status, String code, Object... args) {
        ExceptionInfo info = getMessage(code, args);
        return new CustomerException(info.getCode(), info.getMessage(), status);
    }

    /**
     * 创建异常（默认500状态码）
     *
     * @param code 错误码
     * @param args 消息参数
     * @return 业务异常
     */
    public static CustomerException createException(String code, Object... args) {
        ExceptionInfo info = getMessage(code, args);
        return new CustomerException(info.getCode(), info.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ==================== 异常信息提取 ====================

    /**
     * 从异常对象提取异常信息
     *
     * @param e 异常对象
     * @return 异常信息
     */
    public static ExceptionInfo getMessage(Exception e) {
        if (e instanceof CustomerException ce) {
            return ExceptionInfo.builder()
                    .code(ce.getCode())
                    .message(ce.getMessage())
                    .httpStatus(ce.getHttpStatus().value())
                    .build();
        } else {
            return ExceptionInfo.builder()
                    .code(DEFAULT_ERROR_CODE)
                    .message(e.getMessage() != null ? e.getMessage() : "Internal server error")
                    .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .build();
        }
    }

    /**
     * 根据错误码获取国际化消息
     * 支持从数据库加载业务错误码配置，并根据当前语言环境返回对应的错误消息
     *
     * @param code 错误码
     * @param args 消息参数
     * @return 异常信息
     */
    public static ExceptionInfo getMessage(String code, Object... args) {
        List<ExceptionCode> businessCodes = loadBusinessCodes(code);

        if (CollectionUtils.isEmpty(businessCodes)) {
            return ExceptionInfo.unknown(code);
        }

        Locale locale = I18nMessageUtils.requireLocale();
        ExceptionCode businessCode = businessCodes.stream()
                .filter(bc -> locale.getLanguage().equals(bc.getLanguage()))
                .findFirst()
                .orElse(businessCodes.get(0));

        return ExceptionInfo.builder()
                .code(businessCode.getCode())
                .message(businessCode.getFormatedMessage(args))
                .build();
    }

    /**
     * 从缓存加载业务错误码
     *
     * @param code 错误码
     * @return 业务错误码列表
     */
    private static List<ExceptionCode> loadBusinessCodes(String code) {
        try {
            return BUSINESS_CODE_CACHE.get(code, k -> {
                try {
                    return businessCodeService.list(k);
                } catch (Exception ex) {
                    log.warn("Failed to load business codes for code {}", k, ex);
                    return Collections.emptyList();
                }
            });
        } catch (Exception e) {
            log.warn("Cache retrieval failed for code {}", code, e);
            return Collections.emptyList();
        }
    }
}
