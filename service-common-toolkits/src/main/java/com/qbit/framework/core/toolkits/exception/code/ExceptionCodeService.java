package com.qbit.framework.core.toolkits.exception.code;

/**
 * @author Qbit Framework
 */
public interface ExceptionCodeService {
    String getMessage(String code, Object... args);
}
