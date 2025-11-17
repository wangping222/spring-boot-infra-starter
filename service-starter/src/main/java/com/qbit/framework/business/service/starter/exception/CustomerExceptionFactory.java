package com.qbit.framework.business.service.starter.exception;

/**
 * @description @Date 2025/9/24 14:49
 * @author zhoubobing
 */

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qbit.framework.business.service.starter.i18n.I18nMessageUtils;
import com.qbit.framework.business.service.starter.message.MessageFormatter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author zhoubobing
 */
@Slf4j
public class CustomerExceptionFactory {

    public static final String ERROR = "999999";
    private static BusinessCodeService businessCodeService;
    private static final Cache<String, List<BusinessCodeDTO>> BUSINESS_CODE_CACHE =
            Caffeine.newBuilder()
                    .maximumSize(2048)
                    .expireAfterWrite(Duration.ofMinutes(10))
                    .build();

    public static void setBusinessCodeService(BusinessCodeService businessCodeService) {
        CustomerExceptionFactory.businessCodeService = businessCodeService;
    }

    private CustomerExceptionFactory() {
    }

    public CustomerException createException(String code, Object... args) {
        ExceptionInfo info = getMessage(code, args);
        return new CustomerException(info.getCode(), info.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 业务错误（默认 500 Internal Server Error），按业务码+参数生成消息。
     */
    public static CustomerException business(String code, Object... args) {
        return createException(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

    /**
     * 业务错误（默认 500），直接使用给定消息。
     */
    public static CustomerException businessMessage(String message) {
        String msg = message == null ? "business exception" : message;
        return new CustomerException(DefaultExceptionCode.COMMON_ERROR.getCode(), msg, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 使用指定的 HttpStatus 创建业务异常。
     */
    public static CustomerException createException(HttpStatus status, String code, Object... args) {
        ExceptionInfo info = getMessage(code, args);
        return new CustomerException(info.getCode(), info.getMessage(), status);
    }

    /**
     * 400 Bad Request
     */
    public static CustomerException badRequest(String code, Object... args) {
        return createException(HttpStatus.BAD_REQUEST, code, args);
    }

    /**
     * 401 Unauthorized
     */
    public static CustomerException unauthorized(String code, Object... args) {
        return createException(HttpStatus.UNAUTHORIZED, code, args);
    }

    /**
     * 403 Forbidden
     */
    public static CustomerException forbidden(String code, Object... args) {
        return createException(HttpStatus.FORBIDDEN, code, args);
    }

    /**
     * 404 Not Found
     */
    public static CustomerException notFound(String code, Object... args) {
        return createException(HttpStatus.NOT_FOUND, code, args);
    }

    /**
     * 429 Too Many Requests
     */
    public static CustomerException tooManyRequests(String code, Object... args) {
        return createException(HttpStatus.TOO_MANY_REQUESTS, code, args);
    }

    /**
     * 500 Internal Server Error
     */
    public static CustomerException internalError(String code, Object... args) {
        return createException(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

    /**
     * 将异常转换为统一的异常信息对象。
     */
    public static ExceptionInfo getMessage(Exception e) {
        ExceptionInfo info = new ExceptionInfo();
        if (e instanceof CustomerException ce) {
            info.setCode(ce.getCode());
            info.setMessage(ce.getMessage());
        } else {
            info.setCode(ERROR);
            info.setMessage(e.getMessage() == null ? "internal server error" : e.getMessage());
        }
        return info;
    }

    /**
     * 根据异常码、参数和当前语言环境获取异常消息。
     *
     * @param code 异常码
     * @param args 参数
     * @return 异常信息对象（包含 code 与 message）
     */
    public static ExceptionInfo getMessage(String code, Object... args) {
        // 获取当前语言环境，如果不存在则默认为 "EN_US"
        Locale locale = I18nMessageUtils.requireLocale();

        // 从缓存中尝试获取业务码
        BusinessCodeDTO businessCode;

        // 通过缓存获取业务码列表，失败时返回空列表
        List<BusinessCodeDTO> businessCodes;
        try {
            businessCodes = BUSINESS_CODE_CACHE.get(code, k -> {
                try {
                    return businessCodeService.list(k);
                } catch (Exception ex) {
                    log.warn("Failed to load business codes for code {}", k, ex);
                    return Collections.emptyList();
                }
            });
        } catch (Exception e) {
            log.warn("Cache retrieval failed for code {}", code, e);
            businessCodes = Collections.emptyList();
        }

        if (CollectionUtils.isEmpty(businessCodes)) {
            return ExceptionInfo.unknown(code);
        } else {
            // 根据语言筛选业务码
            businessCode =
                    businessCodes.stream()
                            .filter(s -> locale.getLanguage().equals(s.getLanguage()))
                            .findFirst()
                            .orElse(businessCodes.get(0));
        }

        // 根据业务码的消息模板和参数生成最终的消息
        ExceptionInfo info = new ExceptionInfo();
        info.setCode(businessCode.getCode());
        info.setMessage(MessageFormatter.java().format(businessCode.getMessage(), args));
        return info;
    }


    @Data
    public static class ExceptionInfo {
        private String code;
        private String message;

        public static ExceptionInfo unknown(String code) {
            ExceptionInfo info = new ExceptionInfo();
            info.setCode(ERROR);
            info.setMessage("unknown code：" + code);
            return info;
        }
    }

}
