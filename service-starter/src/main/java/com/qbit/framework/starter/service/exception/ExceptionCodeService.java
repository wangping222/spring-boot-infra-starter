package com.qbit.framework.starter.service.exception;

public interface ExceptionCodeService {
    String getMessage(String code, Object... args);
}
