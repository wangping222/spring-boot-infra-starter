package com.qbit.framework.core.toolkits.exception.factory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qbit.framework.core.toolkits.exception.code.BusinessCodeDTO;
import com.qbit.framework.core.toolkits.exception.code.BusinessCodeService;
import com.qbit.framework.core.toolkits.exception.code.DefaultExceptionCode;
import com.qbit.framework.core.toolkits.exception.type.CustomerException;
import com.qbit.framework.core.toolkits.i18n.I18nMessageUtils;
import com.qbit.framework.core.toolkits.message.MessageFormatter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Qbit Framework
 */
@Slf4j
public class CustomerExceptionFactory {

    public static final String ERROR = "999999";
    private static final Cache<String, List<BusinessCodeDTO>> BUSINESS_CODE_CACHE =
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

    public static CustomerException business(String code, Object... args) {
        return createException(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

    public static CustomerException businessMessage(String message) {
        String msg = message == null ? "business exception" : message;
        return new CustomerException(DefaultExceptionCode.COMMON_ERROR.getCode(), msg, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static CustomerException createException(HttpStatus status, String code, Object... args) {
        ExceptionInfo info = getMessage(code, args);
        return new CustomerException(info.getCode(), info.getMessage(), status);
    }

    public static CustomerException badRequest(String code, Object... args) {
        return createException(HttpStatus.BAD_REQUEST, code, args);
    }

    public static CustomerException unauthorized(String code, Object... args) {
        return createException(HttpStatus.UNAUTHORIZED, code, args);
    }

    public static CustomerException forbidden(String code, Object... args) {
        return createException(HttpStatus.FORBIDDEN, code, args);
    }

    public static CustomerException notFound(String code, Object... args) {
        return createException(HttpStatus.NOT_FOUND, code, args);
    }

    public static CustomerException tooManyRequests(String code, Object... args) {
        return createException(HttpStatus.TOO_MANY_REQUESTS, code, args);
    }

    public static CustomerException internalError(String code, Object... args) {
        return createException(HttpStatus.INTERNAL_SERVER_ERROR, code, args);
    }

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

    public static ExceptionInfo getMessage(String code, Object... args) {
        Locale locale = I18nMessageUtils.requireLocale();

        BusinessCodeDTO businessCode;

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
            businessCode =
                    businessCodes.stream()
                            .filter(s -> locale.getLanguage().equals(s.getLanguage()))
                            .findFirst()
                            .orElse(businessCodes.get(0));
        }

        ExceptionInfo info = new ExceptionInfo();
        info.setCode(businessCode.getCode());
        info.setMessage(MessageFormatter.java().format(businessCode.getMessageTemplate(), args));
        return info;
    }

    public CustomerException createException(String code, Object... args) {
        ExceptionInfo info = getMessage(code, args);
        return new CustomerException(info.getCode(), info.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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
