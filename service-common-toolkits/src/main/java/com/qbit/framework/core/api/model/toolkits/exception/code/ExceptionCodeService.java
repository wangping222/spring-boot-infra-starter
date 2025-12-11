package com.qbit.framework.core.api.model.toolkits.exception.code;

public interface ExceptionCodeService {
    String getMessage(String code, Object... args);
}
