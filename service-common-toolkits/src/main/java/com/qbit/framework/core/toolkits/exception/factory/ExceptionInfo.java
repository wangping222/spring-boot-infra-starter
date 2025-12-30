package com.qbit.framework.core.toolkits.exception.factory;

import lombok.Data;

@Data
public class ExceptionInfo {
    private String code;
    private String message;

    public static ExceptionInfo unknown(String code) {
        ExceptionInfo info = new ExceptionInfo();
        info.setCode(CustomerExceptionFactory.ERROR);
        info.setMessage("unknown code：" + code);
        return info;
    }
}
