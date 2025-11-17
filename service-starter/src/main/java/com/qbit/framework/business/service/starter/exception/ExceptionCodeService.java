package com.qbit.framework.business.service.starter.exception;

public interface ExceptionCodeService {
    String getMessage(String code, Object... args);
}
